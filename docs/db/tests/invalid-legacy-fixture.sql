DO $scope$ BEGIN
 IF current_database() !~ '^dts12_schema_test_[a-z0-9_]{8,60}$' THEN
  RAISE EXCEPTION 'Test fixtures require the runner-owned temporary database';
 END IF;
END; $scope$;

BEGIN;
DELETE FROM dts_result_schema_history WHERE version='V002';
DROP INDEX uq_learning_results_source;
INSERT INTO learning_results(user_id,source_id,target_id,source_type,target_type,attempt_no,result,started_at)
 SELECT user_id,source_id,target_id,source_type,target_type,2,result,started_at FROM learning_results WHERE id='10000000-0000-0000-0000-000000000001';
COMMIT;
