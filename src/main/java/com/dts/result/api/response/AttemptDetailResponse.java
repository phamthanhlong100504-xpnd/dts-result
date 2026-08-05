package com.dts.result.api.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AttemptDetailResponse(
        UUID id,
        UUID tenantId,
        UUID userId,
        UUID nodeId,
        UUID contentId,
        String contentType,
        Integer seqNo,
        OffsetDateTime startedAt,
        OffsetDateTime endedAt,
        OffsetDateTime submittedAt,
        String status,
        String gradingStatus,
        Integer durationSec,
        Integer timeTakenSec,
        String deviceKind,
        UUID sessionId,
        UUID contentVersionId,
        String sourceService,
        String sourceRef,
        BigDecimal score,
        BigDecimal maxScore,
        BigDecimal penaltyScore,
        BigDecimal finalScore,
        Boolean isPassed,
        Boolean isLate,
        Integer hintUsedCount,
        Object answers,
        Object proctoringData,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
