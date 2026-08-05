package com.dts.result.api.form;

import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record RecordTrackingEventRequest(
        @NotNull(message = "eventType is required")
        String eventType,

        @NotNull(message = "entityKind is required")
        String entityKind,

        @NotNull(message = "entityId is required")
        UUID entityId,

        @NotNull(message = "versionId is required")
        UUID versionId,

        @NotNull(message = "versionNo is required")
        Integer versionNo,

        @NotNull(message = "language is required")
        String language,

        UUID blockId,
        String nodePath,

        @NotNull(message = "occurredAt is required")
        OffsetDateTime occurredAt,

        @NotNull(message = "source is required")
        String source,

        @NotNull(message = "idempotencyKey is required")
        String idempotencyKey,

        Map<String, Object> payload,
        Map<String, Object> context
) {
}
