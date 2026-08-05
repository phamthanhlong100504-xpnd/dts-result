package com.dts.result.api.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CriteriaResultResponse(
        UUID id,
        UUID tenantId,
        UUID criteriaId,
        UUID userId,
        Integer seqNo,
        Boolean isLatest,
        String status,
        BigDecimal totalScore,
        Boolean isPassed,
        String gradeLabel,
        Object items,
        UUID graderId,
        String graderNote,
        OffsetDateTime gradedAt,
        OffsetDateTime evaluatedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
