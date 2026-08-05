package com.dts.result.api.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReactionResponse(
        UUID id,
        UUID tenantId,
        UUID userId,
        UUID contentId,
        String contentType,
        String reactionType,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
