-- Run only in an isolated temporary database after apply-schema.psql.
BEGIN;
SET LOCAL search_path=public,pg_catalog;
SET LOCAL statement_timeout='30s';
DO $scope$
BEGIN
 IF current_database() !~ '^dts12_schema_test_[a-z0-9_]{8,60}$' THEN
   RAISE EXCEPTION 'Schema smoke fixtures require an isolated temporary test database';
 END IF;
END;
$scope$;

INSERT INTO learning_results(id,user_id,source_id,target_id,source_type,target_type,attempt_no,result,started_at)
 VALUES('00000000-0000-0000-0000-000000000001',gen_random_uuid(),'00000000-0000-0000-0000-000000000002','00000000-0000-0000-0000-000000000003','EXAM_SESSION','EXAM',1,'PASSED',CURRENT_TIMESTAMP);
INSERT INTO learning_summaries(user_id,target_id,target_type,last_result_id) SELECT user_id,target_id,target_type,id FROM learning_results;
DO $body$
BEGIN
 BEGIN
  INSERT INTO learning_results(user_id,source_id,target_id,source_type,target_type,attempt_no,result,started_at)
  SELECT user_id,source_id,target_id,source_type,target_type,attempt_no,result,started_at FROM learning_results;
  RAISE EXCEPTION 'Duplicate source result accepted';
 EXCEPTION WHEN unique_violation THEN NULL; END;
 BEGIN
  INSERT INTO learning_summaries(user_id,target_id,target_type) SELECT user_id,target_id,target_type FROM learning_summaries;
  RAISE EXCEPTION 'Duplicate summary accepted';
 EXCEPTION WHEN unique_violation THEN NULL; END;
 BEGIN
  UPDATE learning_results SET result='INVALID_RESULT'; RAISE EXCEPTION 'Invalid result enum accepted';
 EXCEPTION WHEN check_violation THEN NULL; END;
END;
$body$;
SELECT json_build_object('schema_smoke_passed',true,'fixture_scope','temporary database; transaction rolled back');
ROLLBACK;
