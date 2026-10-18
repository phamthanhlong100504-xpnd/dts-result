package com.dts.result.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ResultWriteLock {
    private final JdbcTemplate jdbc;

    // Serialize source retries first, then all summaries for this user, across JVMs.
    // The existing unique source index remains the database's final duplicate guard.
    @Transactional(propagation = Propagation.MANDATORY)
    public void acquire(java.util.UUID sourceId, String sourceType, java.util.UUID userId) {
        lock("result-source:" + sourceType + ":" + sourceId);
        lock("result-user:" + userId);
    }
    private void lock(String key) {
        jdbc.query("SELECT pg_advisory_xact_lock(hashtextextended(?::text, 0))",
                (RowCallbackHandler) rs -> {}, key);
    }
}
