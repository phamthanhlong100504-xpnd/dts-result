package com.dts.result.api.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record LearningSummaryResponse(
        UUID id,
        UUID userId,
        UUID targetId,
        UUID lastResultId,
        String targetType,
        Integer attemptCount,
        Integer completionCount,
        BigDecimal bestScore,
        BigDecimal latestScore,
        BigDecimal averageScore,
        BigDecimal progress,
        Integer totalDurationSeconds,
        String status,
        OffsetDateTime lastActivityAt,
        OffsetDateTime completedAt,
        Object summarySnapshot,
        Object metadata,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
