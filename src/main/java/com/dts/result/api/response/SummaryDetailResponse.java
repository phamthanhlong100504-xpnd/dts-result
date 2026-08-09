package com.dts.result.api.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class SummaryDetailResponse {
    private String targetType;
    private UUID targetId;
    private String status;
    private Integer attemptCount;
    private Integer completionCount;
    private BigDecimal bestScore;
    private BigDecimal latestScore;
    private BigDecimal averageScore;
    private BigDecimal progress;
    private Integer totalDurationSeconds;
    private Instant lastActivityAt;
    private Instant completedAt;
    private Map<String, Object> summarySnapshot;
    private Map<String, Object> metadata;
}
