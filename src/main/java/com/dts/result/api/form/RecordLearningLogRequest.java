package com.dts.result.api.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record RecordLearningLogRequest(
        UUID nodeId,

        @NotNull(message = "contentId is required")
        UUID contentId,

        @NotNull(message = "contentType is required")
        String contentType,

        @NotNull(message = "sessionKind is required")
        String sessionKind,

        @NotNull(message = "startedAt is required")
        OffsetDateTime startedAt,

        @NotNull(message = "endedAt is required")
        OffsetDateTime endedAt,

        @NotNull(message = "durationSec is required")
        @Min(value = 0, message = "durationSec must be non-negative")
        Integer durationSec,

        Integer mediaPositionSec,
        Integer documentPageRead,
        String deviceKind,
        Map<String, Object> metadata
) {
}
