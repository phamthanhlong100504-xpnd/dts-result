package com.dts.result.infrastructure.repository;

import com.dts.result.api.response.LeaderboardResponse;
import com.dts.result.api.response.ResultAnalyticsResponse;
import com.dts.result.application.service.ResultBusinessScope.Window;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** A single database snapshot per response; no per-user/per-attempt service calls. */
@Repository
public class PerformanceProjectionRepository {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    public PerformanceProjectionRepository(JdbcTemplate jdbc,ObjectMapper mapper) {this.jdbc=jdbc;this.mapper=mapper;}
    private Timestamp timestamp(Instant value) {return value==null?null:Timestamp.from(value);}
    private <T> T read(Class<T> type,String sql,Object... arguments) {
        String json=jdbc.queryForObject(sql,String.class,arguments);
        try {return mapper.readValue(json,type);}
        catch(JsonProcessingException ex) {throw new IllegalStateException("Projection response mapping failed",ex);}
    }
    public LeaderboardResponse leaderboard(UUID user,String license,Window window,int page,int size) {
        return read(LeaderboardResponse.class,LEADERBOARD,user,license,timestamp(window.from()),
                timestamp(window.effectiveEnd()),timestamp(window.asOf()),page,size,window.period(),timestamp(window.to()));
    }
    public ResultAnalyticsResponse analytics(UUID user,String license,Window window) {
        return read(ResultAnalyticsResponse.class,ANALYTICS,user,license,timestamp(window.from()),
                timestamp(window.effectiveEnd()),timestamp(window.asOf()),window.period());
    }

    private static final String LEADERBOARD="""
        WITH request AS (SELECT ?::uuid who,?::text license,?::timestamptz range_from,
            ?::timestamptz effective_to,?::timestamptz as_of,?::integer page_no,
            ?::integer page_size,?::text period,?::timestamptz range_to),
        credits AS MATERIALIZED (
            SELECT c.* FROM request q CROSS JOIN LATERAL
              public.dts_exp_credits(q.license,q.range_from,q.effective_to) c
        ), totals AS (
            SELECT user_id,SUM(credited_exp)::bigint ranking_score,MAX(credited_at) last_credited_at
            FROM credits GROUP BY user_id
        ), ranked AS (
            SELECT t.*,p.display_name,p.user_id profile_id,
              ROW_NUMBER() OVER(ORDER BY t.ranking_score DESC,t.last_credited_at ASC,t.user_id ASC) position
            FROM totals t LEFT JOIN leaderboard_public_profiles p ON p.user_id=t.user_id
            WHERE t.ranking_score>0 AND COALESCE(p.deleted,FALSE)=FALSE
        ), page_rows AS (
            SELECT * FROM ranked ORDER BY position
            LIMIT (SELECT page_size FROM request)
            OFFSET (SELECT page_no::bigint*page_size FROM request)
        ) SELECT jsonb_build_object(
          'licenseClass',q.license,'period',q.period,'timezone','Asia/Ho_Chi_Minh',
          'asOf',q.as_of,'from',q.range_from,'to',q.range_to,
          'currentUser',jsonb_build_object('userId',q.who,
            'displayName',(SELECT display_name FROM leaderboard_public_profiles WHERE user_id=q.who AND NOT deleted),
            'avatarUrl',NULL,'profileStatus',COALESCE((SELECT CASE WHEN deleted OR display_name IS NULL
                THEN 'UNAVAILABLE' ELSE 'AVAILABLE' END FROM leaderboard_public_profiles WHERE user_id=q.who),'PENDING'),
            'rank',(SELECT position FROM ranked WHERE user_id=q.who),
            'rankingScore',COALESCE((SELECT ranking_score FROM ranked WHERE user_id=q.who),0),
            'lastCreditedAt',(SELECT last_credited_at FROM ranked WHERE user_id=q.who)),
          'items',COALESCE((SELECT jsonb_agg(jsonb_build_object('userId',p.user_id,
            'displayName',p.display_name,'avatarUrl',NULL,
            'profileStatus',CASE WHEN p.profile_id IS NULL THEN 'PENDING' WHEN p.display_name IS NULL THEN 'UNAVAILABLE' ELSE 'AVAILABLE' END,
            'rank',p.position,'rankingScore',p.ranking_score,'lastCreditedAt',p.last_credited_at)
            ORDER BY p.position) FROM page_rows p),'[]'::jsonb),
          'totalParticipants',(SELECT COUNT(*) FROM ranked),'page',q.page_no,'size',q.page_size,
          'totalPages',((SELECT COUNT(*) FROM ranked)+q.page_size-1)/q.page_size,
          'ruleVersion','EXP_2026_01',
          'questionCaptureStartedAt',(SELECT capture_started_at FROM practice_exp_source_state WHERE source_system='dts-practice')
        )::text FROM request q
        """;

    private static final String ANALYTICS="""
        WITH request AS (SELECT ?::uuid who,?::text license,?::timestamptz range_from,
          ?::timestamptz range_to,?::timestamptz as_of,?::text period),
        policy AS (SELECT (SELECT question_count FROM source_exam_scales
          WHERE license_class=(SELECT license FROM request)) scale),
        facts AS MATERIALIZED (
          SELECT e.* FROM screen14_exam_facts e CROSS JOIN request q
          WHERE e.user_id=q.who AND e.license_class=q.license
          AND (q.range_from IS NULL OR e.completed_at>=q.range_from) AND e.completed_at<q.range_to
        ), summary AS (
          SELECT COUNT(*) attempts,COALESCE(SUM(correct_count),0)::bigint correct_count,
            COALESCE(SUM(question_count),0)::bigint question_count,
            CASE WHEN (SELECT scale FROM policy) IS NULL THEN NULL::numeric
              WHEN COUNT(*)=0 THEN 0::numeric ELSE
              ROUND(AVG(correct_count::numeric* (SELECT scale FROM policy)/question_count),2) END average_score,
            ROUND(COALESCE(100.0*COUNT(*) FILTER(WHERE result='PASSED')/NULLIF(COUNT(*),0),0),2) pass_rate,
            ROUND(COALESCE(100.0*SUM(correct_count)/NULLIF(SUM(question_count),0),0),2) accuracy_rate
          FROM facts
        ), daily AS (
          SELECT (completed_at AT TIME ZONE 'Asia/Ho_Chi_Minh')::date AS completed_day,
            ROUND(AVG(correct_count::numeric*(SELECT scale FROM policy)/question_count),2) score,
            COUNT(*) attempts FROM facts GROUP BY 1
        ), bounded_days AS (SELECT * FROM daily ORDER BY completed_day DESC LIMIT 3660),
        recent AS (SELECT * FROM facts ORDER BY completed_at DESC,id DESC LIMIT 5)
        SELECT jsonb_build_object('licenseClass',q.license,'period',q.period,
          'timezone','Asia/Ho_Chi_Minh','asOf',q.as_of,'from',q.range_from,'to',q.range_to,
          'dataStatus',CASE WHEN p.scale IS NULL THEN 'SOURCE_POLICY_PENDING' ELSE 'READY' END,
          'trendMode','DAILY_AVERAGE','scoreTrendTruncated',(SELECT COUNT(*)>3660 FROM daily),
          'scoreTrendTotalDays',(SELECT COUNT(*) FROM daily),'scoreTrendLimit',3660,
          'summary',jsonb_build_object('averageScore',s.average_score,'maxScore',p.scale,
            'scoreBasis','NORMALIZED_CORRECT_COUNT','passRate',s.pass_rate,'accuracyRate',s.accuracy_rate,
            'attempts',s.attempts,'correctCount',s.correct_count,'questionCount',s.question_count),
          'scoreTrend',COALESCE((SELECT jsonb_agg(jsonb_build_object('date',d.completed_day,
            'score',d.score,'averageScore',d.score,'attempts',d.attempts) ORDER BY d.completed_day)
            FROM bounded_days d),'[]'::jsonb),
          'recentResults',COALESCE((SELECT jsonb_agg(jsonb_build_object('resultId',r.id,
            'title',r.display_title,'activityType',r.activity_type,'licenseClass',r.license_class,
            'result',r.result,'score',ROUND(r.correct_count::numeric*p.scale/r.question_count,2),
            'maxScore',p.scale,'sourceScore',r.score,'sourceMaxScore',r.max_score,
            'correctCount',r.correct_count,'questionCount',r.question_count,'completedAt',r.completed_at,
            'resultAvailable',r.result_available,'reviewAvailable',r.review_available,
            'reasonCode',CASE WHEN r.review_available IS NULL THEN 'SOURCE_POLICY_REQUIRED'
                WHEN r.review_available THEN NULL ELSE 'REVIEW_NOT_ALLOWED' END,
            'sourceResult',jsonb_build_object('sourceType',r.source_type,'sourceId',r.source_id,
                'resultPath',(CASE r.source_type WHEN 'PRACTICE_SESSION' THEN '/practice/result/'
                  ELSE '/examination/result/' END)||r.source_id)) ORDER BY r.completed_at DESC,r.id DESC)
            FROM recent r),'[]'::jsonb))::text
        FROM request q CROSS JOIN policy p CROSS JOIN summary s
        """;
}
