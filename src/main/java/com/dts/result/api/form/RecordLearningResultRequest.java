package com.dts.result.api.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record RecordLearningResultRequest(
        @NotNull(message = "sourceId is required")
        UUID sourceId,

        @NotNull(message = "targetId is required")
        UUID targetId,

        @NotNull(message = "sourceType is required")
        String sourceType,

        @NotNull(message = "targetType is required")
        String targetType,

        BigDecimal score,
        BigDecimal maxScore,

        @NotNull(message = "progress is required")
        BigDecimal progress,

        @NotNull(message = "durationSeconds is required")
        @Min(value = 0, message = "durationSeconds must be non-negative")
        Integer durationSeconds,

        @NotNull(message = "result is required")
        String result,

        @NotNull(message = "startedAt is required")
        OffsetDateTime startedAt,

        OffsetDateTime completedAt,
        Map<String, Object> resultSnapshot,
        Map<String, Object> metadata
) {
}
