package com.dts.result.api.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record LearningResultResponse(
        UUID id,
        UUID userId,
        UUID sourceId,
        UUID targetId,
        String sourceType,
        String targetType,
        Integer attemptNo,
        BigDecimal score,
        BigDecimal maxScore,
        BigDecimal progress,
        Integer durationSeconds,
        String result,
        OffsetDateTime startedAt,
        OffsetDateTime completedAt,
        Object resultSnapshot,
        Object metadata,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
