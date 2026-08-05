package com.dts.result.api.form;

import com.dts.result.application.enums.ContentType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record SubmitAttemptRequest(
        UUID nodeId,

        @NotNull(message = "contentId is required")
        UUID contentId,

        @NotNull(message = "contentType is required")
        ContentType contentType,

        @NotNull(message = "startedAt is required")
        OffsetDateTime startedAt,

        @NotNull(message = "submittedAt is required")
        OffsetDateTime submittedAt,

        @NotNull(message = "durationSec is required")
        @Min(value = 0, message = "durationSec must be non-negative")
        Integer durationSec,

        @NotNull(message = "timeTakenSec is required")
        @Min(value = 0, message = "timeTakenSec must be non-negative")
        Integer timeTakenSec,

        String deviceKind,
        UUID sessionId,
        UUID contentVersionId,
        String sourceService,
        String sourceRef,

        @Valid
        List<QuestionAnswerForm> answers,

        Map<String, Object> proctoringData
) {
}
