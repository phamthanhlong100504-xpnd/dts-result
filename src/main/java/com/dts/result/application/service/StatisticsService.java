package com.dts.result.application.service;

import com.dts.result.api.response.AttemptTrend;
import com.dts.result.api.response.CompletionTrend;
import com.dts.result.api.response.ScoreTrend;
import com.dts.result.api.response.StatisticsResponse;
import com.dts.result.api.response.StudyTimeTrend;
import com.dts.result.domain.entity.LearningResultEntity;
import com.dts.result.domain.repository.LearningResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsService {

    private final LearningResultRepository resultRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("UTC"));

    @Transactional(readOnly = true)
    public StatisticsResponse getStatistics(UUID userId, Instant from, Instant to, String interval) {
        log.info("Fetching statistics for userId: {}, from: {}, to: {}, interval: {}", userId, from, to, interval);
        
        Instant start = from != null ? from : Instant.now().minus(30, ChronoUnit.DAYS);
        Instant end = to != null ? to : Instant.now();
        
        // Prevent large queries
        if (start.plus(365, ChronoUnit.DAYS).isBefore(end)) {
            throw new IllegalArgumentException("Date range cannot exceed 365 days");
        }

        List<LearningResultEntity> results = resultRepository.findByUserIdAndCompletedAtBetweenOrderByCompletedAtAsc(userId, start, end);
        
        // In-memory aggregation by date (simplified for DAY interval, assuming UTC for date extraction)
        Map<String, List<LearningResultEntity>> resultsByDate = results.stream()
                .filter(r -> r.getCompletedAt() != null)
                .collect(Collectors.groupingBy(r -> DATE_FORMATTER.format(r.getCompletedAt())));
                
        List<ScoreTrend> scoreTrends = new ArrayList<>();
        List<StudyTimeTrend> studyTimeTrends = new ArrayList<>();
        List<AttemptTrend> attemptTrends = new ArrayList<>();
        List<CompletionTrend> completionTrends = new ArrayList<>();

        for (Map.Entry<String, List<LearningResultEntity>> entry : resultsByDate.entrySet()) {
            String dateStr = entry.getKey();
            List<LearningResultEntity> dailyResults = entry.getValue();
            
            // Score Trend
            double avgScore = dailyResults.stream()
                    .filter(r -> r.getScore() != null)
                    .mapToDouble(r -> r.getScore().doubleValue())
                    .average().orElse(0.0);
            scoreTrends.add(ScoreTrend.builder()
                    .date(dateStr)
                    .averageScore(BigDecimal.valueOf(avgScore).setScale(2, RoundingMode.HALF_UP))
                    .build());
                    
            // Study Time Trend
            long totalSeconds = dailyResults.stream()
                    .mapToLong(LearningResultEntity::getDurationSeconds)
                    .sum();
            studyTimeTrends.add(StudyTimeTrend.builder()
                    .date(dateStr)
                    .durationSeconds(totalSeconds)
                    .build());
                    
            // Attempt Trend
            long totalAttempts = dailyResults.size();
            attemptTrends.add(AttemptTrend.builder()
                    .date(dateStr)
                    .attempts(totalAttempts)
                    .build());
                    
            // Completion Trend
            long completed = dailyResults.stream()
                    .filter(r -> "PASSED".equals(r.getResult()) || "COMPLETED".equals(r.getResult()))
                    .count();
            completionTrends.add(CompletionTrend.builder()
                    .date(dateStr)
                    .completed(completed)
                    .build());
        }
        
        // Sort trends by date (keys are yyyy-MM-dd so string sort works)
        scoreTrends.sort((a, b) -> a.getDate().compareTo(b.getDate()));
        studyTimeTrends.sort((a, b) -> a.getDate().compareTo(b.getDate()));
        attemptTrends.sort((a, b) -> a.getDate().compareTo(b.getDate()));
        completionTrends.sort((a, b) -> a.getDate().compareTo(b.getDate()));

        return StatisticsResponse.builder()
                .scoreTrend(scoreTrends)
                .studyTimeTrend(studyTimeTrends)
                .attemptTrend(attemptTrends)
                .completionTrend(completionTrends)
                .build();
    }
}
