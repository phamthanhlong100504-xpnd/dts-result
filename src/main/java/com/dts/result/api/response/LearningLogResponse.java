package com.dts.result.api.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record LearningLogResponse(
        UUID id,
        UUID tenantId,
        UUID userId,
        UUID nodeId,
        UUID contentId,
        String contentType,
        String sessionKind,
        OffsetDateTime startedAt,
        OffsetDateTime endedAt,
        Integer durationSec,
        Integer mediaPositionSec,
        Integer documentPageRead,
        String deviceKind,
        Object metadata,
        OffsetDateTime createdAt
) {
}
