package com.dts.result.api.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TrackingEventResponse(
        UUID id,
        UUID tenantId,
        UUID userId,
        String eventType,
        String entityKind,
        UUID entityId,
        UUID versionId,
        Integer versionNo,
        String language,
        UUID blockId,
        String nodePath,
        OffsetDateTime occurredAt,
        OffsetDateTime receivedAt,
        String source,
        String idempotencyKey,
        Object payload,
        Object context,
        OffsetDateTime createdAt
) {
}
