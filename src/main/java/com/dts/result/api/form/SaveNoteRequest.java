package com.dts.result.api.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SaveNoteRequest(
        @NotNull(message = "contentId is required")
        UUID contentId,

        UUID nodeId,
        UUID contentVersionId,
        Integer mediaTimestampSec,
        Integer documentPage,

        @NotBlank(message = "noteText cannot be blank")
        String noteText
) {
}
