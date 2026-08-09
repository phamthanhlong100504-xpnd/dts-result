package com.dts.result.api.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class RecentActivityResponse {
    private String targetType;
    private UUID targetId;
    private String result;
    private BigDecimal score;
    private Instant completedAt;
}
