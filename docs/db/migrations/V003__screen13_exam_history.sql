-- Keep applied V001/V002 unchanged. Source identity, not attempt_no, defines an exam result.
ALTER TABLE public.learning_results
    ADD COLUMN activity_type VARCHAR(30), ADD COLUMN display_title VARCHAR(200),
    ADD COLUMN license_class VARCHAR(10), ADD COLUMN correct_count INTEGER,
    ADD COLUMN question_count INTEGER, ADD COLUMN result_available BOOLEAN,
    ADD COLUMN review_available BOOLEAN, ADD COLUMN event_version INTEGER;
ALTER TABLE public.learning_results ADD CONSTRAINT ck_history_activity
    CHECK(activity_type IS NULL OR activity_type IN ('PRESET_EXAM','RANDOM_EXAM','OFFICIAL_EXAM'));
ALTER TABLE public.learning_results ADD CONSTRAINT ck_history_counts
    CHECK((correct_count IS NULL OR correct_count>=0) AND (question_count IS NULL OR question_count>0)
        AND (correct_count IS NULL OR question_count IS NULL OR correct_count<=question_count));
ALTER TABLE public.learning_results ADD CONSTRAINT ck_history_release
    CHECK(result_available IS DISTINCT FROM TRUE OR target_type<>'EXAM'
        OR (result IN ('PASSED','FAILED') AND completed_at IS NOT NULL));

-- v1 metadata stays NULL unless actually carried by a source snapshot.
-- In particular, weighted scores are never converted to question counts.
ALTER TABLE public.learning_results DISABLE TRIGGER trg_learning_results_updated_at;
UPDATE public.learning_results SET event_version=1,
    result_available=(result IN ('PASSED','FAILED') AND completed_at IS NOT NULL),
    license_class=CASE WHEN result_snapshot->>'licenseClass' ~ '^[A-Z0-9]{1,10}$'
        THEN result_snapshot->>'licenseClass' ELSE NULL END,
    question_count=CASE WHEN result_snapshot->>'totalQuestions' ~ '^[1-9][0-9]{0,5}$'
        THEN (result_snapshot->>'totalQuestions')::integer ELSE NULL END,
    correct_count=CASE WHEN result_snapshot->>'correctCount' ~ '^[0-9]{1,6}$'
        AND result_snapshot->>'totalQuestions' ~ '^[1-9][0-9]{0,5}$'
        THEN CASE WHEN (result_snapshot->>'correctCount')::integer <= (result_snapshot->>'totalQuestions')::integer
            THEN (result_snapshot->>'correctCount')::integer ELSE NULL END ELSE NULL END
WHERE target_type='EXAM';
ALTER TABLE public.learning_results ENABLE TRIGGER trg_learning_results_updated_at;

-- The same exam has attempt 1 in more than one published version. Preserve each
-- source's number and keep the old uniqueness rule for all non-exam activities.
DROP INDEX public.uq_learning_results_user_target_attempt;
CREATE UNIQUE INDEX uq_learning_results_user_target_attempt
    ON public.learning_results(user_id,target_type,target_id,attempt_no) WHERE target_type<>'EXAM';
CREATE INDEX ix_history_user_completed ON public.learning_results(user_id,completed_at DESC,id DESC);
CREATE INDEX ix_history_exam_filters ON public.learning_results
    (user_id,activity_type,license_class,completed_at DESC,id DESC)
    WHERE target_type='EXAM' AND result_available IS TRUE;
