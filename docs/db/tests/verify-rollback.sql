DO $scope$ BEGIN
 IF current_database() !~ '^dts12_schema_test_[a-z0-9_]{8,60}$' THEN
  RAISE EXCEPTION 'Test fixtures require the runner-owned temporary database';
 END IF;
END; $scope$;

DO $test$ BEGIN
 IF EXISTS(SELECT 1 FROM dts_result_schema_history WHERE version='V002') OR
  (SELECT md5((to_jsonb(r)-ARRAY['activity_type','display_title','license_class','correct_count','question_count','result_available','review_available','event_version'])::text) FROM learning_results r WHERE id='10000000-0000-0000-0000-000000000001')
   IS DISTINCT FROM (SELECT hash FROM dts12_test_preservation) OR
  (SELECT count(*) FROM learning_results WHERE source_id='10000000-0000-0000-0000-000000000003')<>2 THEN
  RAISE EXCEPTION 'Failure did not preserve duplicate records/history';
 END IF;
END; $test$;
