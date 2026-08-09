package com.dts.result.api.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ResumeResponse {
    private String targetType;
    private UUID targetId;
    private BigDecimal progress;
    private Instant lastActivityAt;
}
