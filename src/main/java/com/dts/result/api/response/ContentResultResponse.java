package com.dts.result.api.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ContentResultResponse(
        UUID id,
        UUID tenantId,
        UUID userId,
        UUID contentId,
        String contentType,
        UUID contentVersionId,
        UUID parentNodeId,
        String contentCode,
        String status,
        Integer percent,
        Integer totalLearnSec,
        Integer learnCount,
        Integer attemptCount,
        BigDecimal bestScore,
        BigDecimal lastScore,
        OffsetDateTime lastScoreAt,
        UUID lastAttemptId,
        Object answers,
        Object metadata,
        OffsetDateTime startedAt,
        OffsetDateTime lastActivityAt,
        OffsetDateTime completedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
