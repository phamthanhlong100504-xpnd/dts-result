package com.dts.result.application.dto;

import com.dts.result.application.enums.ContentType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record SubmitAttemptCommand(
        UUID tenantId,
        UUID userId,
        UUID nodeId,
        UUID contentId,
        ContentType contentType,
        OffsetDateTime startedAt,
        OffsetDateTime submittedAt,
        Integer durationSec,
        Integer timeTakenSec,
        String deviceKind,
        UUID sessionId,
        UUID contentVersionId,
        String sourceService,
        String sourceRef,
        List<QuestionAnswerDto> answers,
        Map<String, Object> proctoringData
) {
}
