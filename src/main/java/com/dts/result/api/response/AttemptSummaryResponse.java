package com.dts.result.api.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AttemptSummaryResponse(
        UUID id,
        UUID tenantId,
        UUID userId,
        UUID nodeId,
        UUID contentId,
        String contentType,
        Integer seqNo,
        OffsetDateTime startedAt,
        OffsetDateTime submittedAt,
        String status,
        String gradingStatus,
        BigDecimal score,
        BigDecimal maxScore,
        BigDecimal finalScore,
        Boolean isPassed,
        Integer timeTakenSec,
        OffsetDateTime createdAt
) {
}
