package com.dts.result.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressResponse {
    private String targetType;
    private long total;
    private long completed;
    private long inProgress;
    private long notStarted;
    private BigDecimal completionRate;
    private BigDecimal averageProgress;
}
