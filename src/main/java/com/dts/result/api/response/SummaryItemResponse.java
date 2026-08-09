package com.dts.result.api.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class SummaryItemResponse {
    private String targetType;
    private UUID targetId;
    private String status;
    private Integer attemptCount;
    private BigDecimal bestScore;
    private BigDecimal latestScore;
    private BigDecimal averageScore;
    private BigDecimal progress;
    private Instant lastActivityAt;
}
