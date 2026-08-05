package com.dts.result.api.response;

import com.dts.result.application.enums.ContentType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record SubmitAttemptResponse(
        UUID id,
        UUID tenantId,
        UUID userId,
        UUID contentId,
        ContentType contentType,
        Integer seqNo,
        OffsetDateTime startedAt,
        OffsetDateTime submittedAt,
        String status,
        String gradingStatus,
        BigDecimal score,
        BigDecimal maxScore,
        BigDecimal penaltyScore,
        BigDecimal finalScore,
        Boolean isPassed,
        Integer timeTakenSec,
        OffsetDateTime createdAt
) {
}
