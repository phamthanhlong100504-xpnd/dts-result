package com.dts.result.api.form;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ToggleBookmarkRequest(
        @NotNull(message = "contentId is required")
        UUID contentId,

        @NotNull(message = "contentType is required")
        String contentType,

        UUID nodeId,
        String note
) {
}
