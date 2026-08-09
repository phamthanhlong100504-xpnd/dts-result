package com.dts.result.api.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class HistoryItemResponse {
    private UUID id;
    private String targetType;
    private UUID targetId;
    private Integer attemptNo;
    private String result;
    private BigDecimal score;
    private BigDecimal maxScore;
    private BigDecimal progress;
    private Integer durationSeconds;
    private Instant completedAt;
}
