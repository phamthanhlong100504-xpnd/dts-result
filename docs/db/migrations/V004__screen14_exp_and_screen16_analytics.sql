-- Additive projection only. Existing results, summaries and executed migrations are unchanged.
CREATE TABLE public.study_question_facts (
 event_id UUID PRIMARY KEY,
 user_id UUID NOT NULL,
 license_class VARCHAR(10) NOT NULL,
 bank_version VARCHAR(64) NOT NULL,
 question_id INTEGER NOT NULL CHECK(question_id>0),
 context VARCHAR(40) NOT NULL CHECK(context IN
  ('CHAPTER_LEARNING','CHAPTER_REVIEW','CRITICAL_PRACTICE','WRONG_REVIEW','PRACTICE_MODE')),
 occurred_at TIMESTAMPTZ NOT NULL,
 capture_started_at TIMESTAMPTZ NOT NULL,
 received_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CHECK(occurred_at>=capture_started_at)
);
CREATE INDEX ix_study_question_period ON public.study_question_facts
 (license_class,occurred_at,user_id,bank_version,question_id,event_id);

-- An append-only exclusion/restoration journal. No mutation of the original History facts.
CREATE TABLE public.result_projection_adjustments (
 adjustment_sequence BIGINT GENERATED ALWAYS AS IDENTITY UNIQUE,
 event_id UUID PRIMARY KEY,
 source_type VARCHAR(30) NOT NULL CHECK(source_type IN
  ('PRACTICE_SESSION','EXAM_SESSION','PRACTICE_QUESTION')),
 source_id UUID NOT NULL,
 excluded BOOLEAN NOT NULL,
 reason_code VARCHAR(40) NOT NULL CHECK(reason_code IN
  ('VERIFIED_TEST_DATA','SOURCE_REVOKED','SOURCE_RESTORED')),
 evidence_ref VARCHAR(500) NOT NULL CHECK(length(btrim(evidence_ref))>0),
 recorded_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX ix_projection_adjustment_source ON public.result_projection_adjustments
 (source_type,source_id,adjustment_sequence DESC);
CREATE VIEW public.result_projection_exclusions AS
 SELECT source_type,source_id,reason_code,evidence_ref FROM (
  SELECT DISTINCT ON(source_type,source_id) * FROM public.result_projection_adjustments
  ORDER BY source_type,source_id,adjustment_sequence DESC
 ) latest WHERE excluded;

CREATE FUNCTION public.dts_preserve_projection_fact() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN RAISE EXCEPTION 'Projection evidence is append-only; record a new correction instead'; END;
$$;
CREATE TRIGGER trg_study_question_immutable BEFORE UPDATE OR DELETE ON public.study_question_facts
 FOR EACH ROW EXECUTE FUNCTION public.dts_preserve_projection_fact();
CREATE TRIGGER trg_projection_adjustment_immutable BEFORE UPDATE OR DELETE ON public.result_projection_adjustments
 FOR EACH ROW EXECUTE FUNCTION public.dts_preserve_projection_fact();

CREATE TABLE public.practice_exp_policy_events (
 event_id UUID PRIMARY KEY,
 capture_started_at TIMESTAMPTZ NOT NULL,
 policy_revision BIGINT NOT NULL CHECK(policy_revision>0),
 policy_observed_at TIMESTAMPTZ NOT NULL,
 bank_version VARCHAR(64) NOT NULL,
 question_counts JSONB NOT NULL CHECK(jsonb_typeof(question_counts)='object' AND question_counts<>'{}'::jsonb),
 received_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CHECK(policy_observed_at>=capture_started_at)
);
CREATE INDEX ix_exp_policy_latest ON public.practice_exp_policy_events
 (capture_started_at DESC,policy_revision DESC,policy_observed_at DESC,event_id DESC);
CREATE TRIGGER trg_exp_policy_immutable BEFORE UPDATE OR DELETE ON public.practice_exp_policy_events
 FOR EACH ROW EXECUTE FUNCTION public.dts_preserve_projection_fact();
CREATE VIEW public.practice_exp_source_state AS
 SELECT 'dts-practice'::text source_system,capture_started_at,policy_revision,
  policy_observed_at,event_id policy_event_id,bank_version FROM public.practice_exp_policy_events
 ORDER BY capture_started_at DESC,policy_revision DESC,policy_observed_at DESC,event_id DESC LIMIT 1;
CREATE VIEW public.source_exam_scales AS
 SELECT s.key AS license_class,(s.value::text)::integer question_count,
  p.bank_version,p.policy_observed_at AS observed_at,p.event_id AS source_event_id
 FROM public.practice_exp_source_state latest JOIN public.practice_exp_policy_events p
  ON p.event_id=latest.policy_event_id CROSS JOIN LATERAL jsonb_each(p.question_counts) s;
-- Identity owns names. Only minimal public profile data is projected; never email/phone/token.
CREATE TABLE public.leaderboard_public_profiles (
 user_id UUID PRIMARY KEY,
 display_name VARCHAR(255),
 deleted BOOLEAN NOT NULL DEFAULT FALSE,
 source_timestamp BIGINT NOT NULL,
 source_event_order SMALLINT NOT NULL,
 source_partition INTEGER NOT NULL,
 source_offset BIGINT NOT NULL,
 updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX ix_exam_analytics_scope ON public.learning_results
 (user_id,license_class,completed_at DESC,id DESC)
 WHERE target_type='EXAM' AND result_available IS TRUE;
CREATE INDEX ix_exam_exp_scope ON public.learning_results
 (license_class,activity_type,completed_at,user_id,target_id)
 WHERE target_type='EXAM' AND result_available IS TRUE;

CREATE VIEW public.screen14_exam_facts AS
 SELECT r.* FROM public.learning_results r
 WHERE r.target_type='EXAM' AND r.result_available IS TRUE
 AND r.result IN ('PASSED','FAILED') AND r.event_version=2
 AND r.started_at IS NOT NULL AND r.completed_at>=r.started_at
 AND r.correct_count>=0 AND r.question_count>0 AND r.correct_count<=r.question_count
 AND r.score>=0 AND r.max_score>0 AND r.score<=r.max_score
 AND ((r.source_type='EXAM_SESSION' AND r.activity_type='OFFICIAL_EXAM')
   OR (r.source_type='PRACTICE_SESSION' AND r.activity_type IN ('RANDOM_EXAM','PRESET_EXAM')))
 AND NOT EXISTS(SELECT 1 FROM public.result_projection_exclusions x
   WHERE x.source_type=r.source_type AND x.source_id=r.source_id);

-- Deterministic credit ledger derived from immutable/source-owned facts.
-- Scope-specific preset/question credits are recomputed, never added a second time on replay.
CREATE FUNCTION public.dts_exp_credits(p_license TEXT,p_from TIMESTAMPTZ,p_to TIMESTAMPTZ)
RETURNS TABLE(user_id UUID,source_type TEXT,source_id UUID,activity_type TEXT,
 credited_exp BIGINT,credited_at TIMESTAMPTZ,rule_version TEXT)
LANGUAGE sql STABLE AS $body$
 WITH all_exams AS (
  SELECT e.* FROM public.screen14_exam_facts e WHERE e.license_class=p_license
   AND (p_to IS NULL OR e.completed_at<p_to)
 ), scoped AS (
  SELECT * FROM all_exams WHERE p_from IS NULL OR completed_at>=p_from
 ), officials AS (
  SELECT e.*,ROW_NUMBER() OVER(PARTITION BY e.user_id,e.target_id
   ORDER BY e.completed_at,e.source_id) AS credit_attempt FROM all_exams e
  WHERE e.activity_type='OFFICIAL_EXAM'
 ), randoms AS (
  SELECT e.*,ROW_NUMBER() OVER(PARTITION BY e.user_id,
   (e.completed_at AT TIME ZONE 'Asia/Ho_Chi_Minh')::date
   ORDER BY e.completed_at,e.source_id) AS daily_attempt FROM scoped e
  WHERE e.activity_type='RANDOM_EXAM'
 ), presets AS (
  SELECT e.*,ROW_NUMBER() OVER(PARTITION BY e.user_id,e.target_id
   ORDER BY (e.correct_count::bigint*2+CASE WHEN e.result='PASSED' THEN 30 ELSE 0 END) DESC,
   e.score/e.max_score DESC,(e.result='PASSED') DESC,
   e.completed_at,e.source_id) AS best_attempt FROM scoped e
  WHERE e.activity_type='PRESET_EXAM'
 ), questions AS (
  SELECT q.*,ROW_NUMBER() OVER(PARTITION BY q.user_id,q.bank_version,q.question_id
   ORDER BY q.occurred_at,q.event_id) AS first_correct
  FROM public.study_question_facts q WHERE q.license_class=p_license
   AND (p_from IS NULL OR q.occurred_at>=p_from) AND (p_to IS NULL OR q.occurred_at<p_to)
   AND NOT EXISTS(SELECT 1 FROM public.result_projection_exclusions x
    WHERE x.source_type='PRACTICE_QUESTION' AND x.source_id=q.event_id)
 ), credits AS (
  SELECT o.user_id,o.source_type::text,o.source_id,o.activity_type::text,
   o.correct_count::bigint*10+CASE WHEN o.result='PASSED' THEN 200 ELSE 0 END AS points,
   o.completed_at AS earned_at FROM officials o WHERE o.credit_attempt<=2
    AND (p_from IS NULL OR o.completed_at>=p_from)
  UNION ALL
  SELECT r.user_id,r.source_type::text,r.source_id,r.activity_type::text,
   r.correct_count::bigint*2+CASE WHEN r.result='PASSED' THEN 30 ELSE 0 END,r.completed_at
   FROM randoms r WHERE r.daily_attempt<=5
  UNION ALL
  SELECT p.user_id,p.source_type::text,p.source_id,p.activity_type::text,
   p.correct_count::bigint*2+CASE WHEN p.result='PASSED' THEN 30 ELSE 0 END,p.completed_at
   FROM presets p WHERE p.best_attempt=1
  UNION ALL
  SELECT q.user_id,'PRACTICE_QUESTION',q.event_id,q.context::text,1::bigint,q.occurred_at
   FROM questions q WHERE q.first_correct=1
 ) SELECT c.user_id,c.source_type,c.source_id,c.activity_type,c.points,c.earned_at,'EXP_2026_01'::text
 FROM credits c WHERE c.points>0
$body$;
