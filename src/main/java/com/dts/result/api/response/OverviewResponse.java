package com.dts.result.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverviewResponse {
    private long completedPrograms;
    private long inProgressPrograms;
    private long completedChapters;
    private long completedLessons;
    private BigDecimal averageScore;
    private BigDecimal bestScore;
    private long totalLearningTimeSeconds;
    private long totalAttempts;
    private Instant lastActivityAt;
}
