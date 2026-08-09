package com.dts.result.api.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class HistoryDetailResponse {
    private UUID id;
    private String targetType;
    private UUID targetId;
    private Integer attemptNo;
    private String result;
    private BigDecimal score;
    private BigDecimal maxScore;
    private BigDecimal progress;
    private Integer durationSeconds;
    private Instant startedAt;
    private Instant completedAt;
    private Map<String, Object> resultSnapshot;
    private Map<String, Object> metadata;
}
