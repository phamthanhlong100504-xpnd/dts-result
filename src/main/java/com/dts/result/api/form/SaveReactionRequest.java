package com.dts.result.api.form;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SaveReactionRequest(
        @NotNull(message = "contentId is required")
        UUID contentId,

        @NotNull(message = "contentType is required")
        String contentType,

        @NotNull(message = "reactionType is required")
        String reactionType
) {
}
