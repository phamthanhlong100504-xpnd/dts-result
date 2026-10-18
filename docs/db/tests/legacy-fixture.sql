DO $scope$ BEGIN
 IF current_database() !~ '^dts12_schema_test_[a-z0-9_]{8,60}$' THEN
  RAISE EXCEPTION 'Test fixtures require the runner-owned temporary database';
 END IF;
END; $scope$;

BEGIN;
DELETE FROM dts_result_schema_history WHERE version IN ('V002','V003','V004');
-- Remove only this owned temporary database's new projection schema before simulating V001.
DROP FUNCTION dts_exp_credits(text,timestamptz,timestamptz);
DROP VIEW screen14_exam_facts,source_exam_scales,practice_exp_source_state,result_projection_exclusions;
DROP TABLE study_question_facts,practice_exp_policy_events,leaderboard_public_profiles,result_projection_adjustments;
DROP FUNCTION dts_preserve_projection_fact();
DROP INDEX ix_exam_analytics_scope;
DROP INDEX ix_exam_exp_scope;
ALTER TABLE learning_results DROP COLUMN activity_type, DROP COLUMN display_title, DROP COLUMN license_class,
 DROP COLUMN correct_count, DROP COLUMN question_count, DROP COLUMN result_available, DROP COLUMN review_available, DROP COLUMN event_version;
DROP INDEX ix_history_user_completed;
DROP INDEX uq_learning_results_source;
DROP INDEX uq_learning_results_user_target_attempt;
DROP INDEX uq_learning_summaries_user_target;
INSERT INTO learning_results(id,user_id,source_id,target_id,source_type,target_type,attempt_no,result,started_at,metadata)
 VALUES('10000000-0000-0000-0000-000000000001','10000000-0000-0000-0000-000000000002','10000000-0000-0000-0000-000000000003','10000000-0000-0000-0000-000000000004','EXAM_SESSION','EXAM',1,'PASSED',CURRENT_TIMESTAMP,'{"keep":"original"}');
CREATE TABLE dts12_test_preservation(hash text NOT NULL);
INSERT INTO dts12_test_preservation SELECT md5(to_jsonb(r)::text) FROM learning_results r WHERE id='10000000-0000-0000-0000-000000000001';
COMMIT;
