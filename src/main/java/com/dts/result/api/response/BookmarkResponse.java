package com.dts.result.api.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BookmarkResponse(
        UUID id,
        UUID tenantId,
        UUID userId,
        UUID contentId,
        String contentType,
        UUID nodeId,
        String note,
        Boolean isBookmarked,
        OffsetDateTime createdAt
) {
}
