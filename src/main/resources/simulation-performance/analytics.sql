WITH request AS(SELECT ?::uuid who,?::text license,?::timestamptz range_from,?::timestamptz range_to,
 ?::timestamptz as_of,?::bigint watermark,?::text period),
facts AS MATERIALIZED(
 SELECT 'THEORY'::text module,e.id result_id,e.source_type,e.source_id,e.activity_type,e.display_title,
  e.license_class,e.score source_score,e.max_score source_max_score,
  e.correct_count::numeric*100/e.question_count normalized_score,e.result,e.completed_at,
  e.result_available,e.review_available,
  (CASE e.source_type WHEN 'PRACTICE_SESSION' THEN '/practice/result/' ELSE '/examination/result/' END)||e.source_id result_path,
  NULL::text review_path,NULL::uuid preset_set_id,NULL::uuid preset_version_id
 FROM screen14_exam_facts e CROSS JOIN request q WHERE e.user_id=q.who AND e.license_class=q.license
  AND (q.range_from IS NULL OR e.completed_at>=q.range_from) AND e.completed_at<q.range_to
 UNION ALL
 SELECT 'SIMULATION',s.source_id,'SIMULATION_SESSION',s.source_id,s.activity_type,s.display_title,s.license_class,
  s.source_score::numeric,s.source_max_score::numeric,s.source_score::numeric*100/s.source_max_score,s.result,s.completed_at,
  TRUE,TRUE,'/practice/simulations/exams/'||s.source_id||'/result','/practice/simulations/exams/'||s.source_id||'/review',s.preset_set_id,s.preset_version_id
 FROM simulation_performance_exam_facts s CROSS JOIN request q WHERE s.user_id=q.who AND s.license_class=q.license
  AND s.event_sequence<=q.watermark AND (q.range_from IS NULL OR s.completed_at>=q.range_from) AND s.completed_at<q.range_to
), summary AS(SELECT COUNT(*) attempts,ROUND(AVG(normalized_score),2) average_score,
 ROUND(100.0*COUNT(*) FILTER(WHERE result='PASSED')/NULLIF(COUNT(*),0),2) pass_rate,
 COUNT(*) FILTER(WHERE module='THEORY') theory_attempts,COUNT(*) FILTER(WHERE module='SIMULATION') simulation_attempts FROM facts),
credits AS MATERIALIZED(SELECT c.* FROM request q CROSS JOIN LATERAL dts_combined_exp_credits(q.license,q.range_from,q.range_to,q.watermark)c WHERE c.user_id=q.who),
exp AS(SELECT COALESCE(SUM(credited_exp),0) total_exp,
 COALESCE(SUM(credited_exp) FILTER(WHERE rule_version='EXP_2026_01'),0) theory_exp,
 COALESCE(SUM(credited_exp) FILTER(WHERE rule_version='SIMULATION_EXP_V1'),0) simulation_exp,
 md5(COALESCE(string_agg(source_type||':'||source_id||':'||credited_exp||':'||(credited_at AT TIME ZONE 'UTC')::text,',' ORDER BY source_type,source_id),'')) fingerprint FROM credits),
daily AS(SELECT(completed_at AT TIME ZONE 'Asia/Ho_Chi_Minh')::date AS completed_day,ROUND(AVG(normalized_score),2) score,COUNT(*) attempts FROM facts GROUP BY 1),
bounded_days AS(SELECT * FROM daily ORDER BY completed_day DESC LIMIT 3660),recent AS(SELECT * FROM facts ORDER BY completed_at DESC,source_type,source_id DESC LIMIT 5)
SELECT jsonb_build_object('licenseClass',q.license,'period',q.period,'performanceMode','COMBINED','timezone','Asia/Ho_Chi_Minh',
 'from',q.range_from,'to',q.range_to,'asOf',q.as_of,'ruleVersion','COMBINED_EXP_V1','scoreBasis','NORMALIZED_PERCENT_100',
 'summary',jsonb_build_object('averageScore',s.average_score,'maxScore',100,'scoreBasis','NORMALIZED_PERCENT_100',
  'passRate',s.pass_rate,'totalExp',e.total_exp,'attempts',s.attempts,'theoryAttempts',s.theory_attempts,
  'simulationAttempts',s.simulation_attempts,'theoryExp',e.theory_exp,'simulationExp',e.simulation_exp),
 'expScope',jsonb_build_object('licenseClass',q.license,'from',q.range_from,'to',q.range_to,'asOf',q.as_of,'sourceWatermark',q.watermark,'creditFingerprint',e.fingerprint,'ruleVersion','COMBINED_EXP_V1'),
 'scoreTrendTruncated',(SELECT COUNT(*)>3660 FROM daily),'scoreTrendTotalDays',(SELECT COUNT(*) FROM daily),'scoreTrendLimit',3660,
 'scoreTrend',COALESCE((SELECT jsonb_agg(jsonb_build_object('date',d.completed_day,'averageScore',d.score,'score',d.score,'attempts',d.attempts)ORDER BY d.completed_day)FROM bounded_days d),'[]'::jsonb),
 'recentResults',COALESCE((SELECT jsonb_agg(jsonb_build_object('module',r.module,'resultId',r.result_id,'sourceType',r.source_type,'sourceId',r.source_id,
  'activityType',r.activity_type,'displayTitle',r.display_title,'licenseClass',r.license_class,'sourceScore',r.source_score,
  'sourceMaxScore',r.source_max_score,'normalizedScore',ROUND(r.normalized_score,2),'result',r.result,'completedAt',r.completed_at,
  'resultAvailable',r.result_available,'reviewAvailable',r.review_available,'presetSetId',r.preset_set_id,'presetVersionId',r.preset_version_id,
  'sourceResult',jsonb_build_object('sourceType',r.source_type,'sourceId',r.source_id,'licenseClass',r.license_class,'resultPath',r.result_path,'reviewPath',r.review_path))
  ORDER BY r.completed_at DESC,r.source_type,r.source_id DESC)FROM recent r),'[]'::jsonb))::text FROM request q CROSS JOIN summary s CROSS JOIN exp e
