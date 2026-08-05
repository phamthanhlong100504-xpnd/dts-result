package com.dts.result.api.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record LessonNoteResponse(
        UUID id,
        UUID tenantId,
        UUID userId,
        UUID contentId,
        UUID nodeId,
        UUID contentVersionId,
        Integer mediaTimestampSec,
        Integer documentPage,
        String noteText,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
