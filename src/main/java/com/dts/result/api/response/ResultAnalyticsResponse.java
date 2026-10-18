package com.dts.result.api.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ResultAnalyticsResponse(String licenseClass, String period, String timezone,
        Instant asOf, Instant from, Instant to, String dataStatus, String trendMode,
        boolean scoreTrendTruncated, long scoreTrendTotalDays, int scoreTrendLimit,
        Summary summary, List<TrendPoint> scoreTrend, List<RecentResult> recentResults) {
    public record Summary(BigDecimal averageScore, Integer maxScore, String scoreBasis,
            BigDecimal passRate, BigDecimal accuracyRate, long attempts,
            long correctCount, long questionCount) {}
    public record TrendPoint(String date, BigDecimal score, BigDecimal averageScore, long attempts) {}
    public record RecentResult(UUID resultId, String title, String activityType, String licenseClass,
            String result, BigDecimal score, Integer maxScore,
            BigDecimal sourceScore, BigDecimal sourceMaxScore,
            Integer correctCount, Integer questionCount, Instant completedAt,
            Boolean resultAvailable, Boolean reviewAvailable, String reasonCode,
            SourceResultResponse sourceResult) {}
}
