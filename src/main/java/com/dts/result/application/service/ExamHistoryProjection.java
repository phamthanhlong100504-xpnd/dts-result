package com.dts.result.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExamHistoryProjection {
    private final JdbcTemplate jdbc;

    // Caller holds ResultWriteLock. Rebuild from persisted facts, so late events
    // and enrichment of an existing source never increment a summary twice.
    @Transactional(propagation = Propagation.MANDATORY)
    public void rebuild(UUID user, UUID target) {
        jdbc.update("""
            WITH facts AS (
                SELECT * FROM learning_results
                WHERE user_id = ? AND target_type = 'EXAM' AND target_id = ?
                  AND completed_at IS NOT NULL AND result IN ('PASSED','FAILED')
                  AND result_available IS TRUE
            ), latest AS (
                SELECT * FROM facts ORDER BY completed_at DESC, id DESC LIMIT 1
            ), totals AS (
                SELECT COUNT(*)::integer AS attempts,
                    COUNT(*) FILTER (WHERE result = 'PASSED')::integer AS completions,
                    MAX(score) AS best, ROUND(AVG(score), 2) AS average,
                    LEAST(COALESCE(SUM(duration_seconds),0),2147483647)::integer AS duration,
                    MIN(completed_at) FILTER (WHERE result = 'PASSED') AS first_pass
                FROM facts
            )
            INSERT INTO learning_summaries (id,user_id,target_type,target_id,last_result_id,
                attempt_count,completion_count,best_score,latest_score,average_score,
                progress,total_duration_seconds,status,last_activity_at,completed_at,
                summary_snapshot,metadata,created_at,updated_at)
            SELECT gen_random_uuid(),l.user_id,'EXAM',l.target_id,l.id,t.attempts,
                t.completions,t.best,l.score,t.average,100,t.duration,
                CASE WHEN t.completions > 0 THEN 'COMPLETED' ELSE 'IN_PROGRESS' END,
                l.completed_at,t.first_pass,l.result_snapshot,l.metadata,NOW(),NOW()
            FROM latest l CROSS JOIN totals t
            ON CONFLICT (user_id,target_type,target_id) DO UPDATE SET
                last_result_id=EXCLUDED.last_result_id,attempt_count=EXCLUDED.attempt_count,
                completion_count=EXCLUDED.completion_count,best_score=EXCLUDED.best_score,
                latest_score=EXCLUDED.latest_score,average_score=EXCLUDED.average_score,
                progress=EXCLUDED.progress,total_duration_seconds=EXCLUDED.total_duration_seconds,
                status=EXCLUDED.status,last_activity_at=EXCLUDED.last_activity_at,
                completed_at=EXCLUDED.completed_at,summary_snapshot=EXCLUDED.summary_snapshot,
                metadata=EXCLUDED.metadata,updated_at=NOW()
            """, user, target);
        // A legacy targetId may have been a versionId; remove only its empty EXAM projection.
        jdbc.update("""
            DELETE FROM learning_summaries s WHERE s.user_id=? AND s.target_type='EXAM'
              AND s.target_id=? AND NOT EXISTS (
                SELECT 1 FROM learning_results r WHERE r.user_id=s.user_id
                  AND r.target_type=s.target_type AND r.target_id=s.target_id)
            """,user,target);
    }
}
