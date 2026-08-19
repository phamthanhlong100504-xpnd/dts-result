package com.dts.result.application.service;

import com.dts.result.api.response.OverviewResponse;
import com.dts.result.domain.repository.LearningSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OverviewService {

    private final LearningSummaryRepository summaryRepository;

    @Transactional(readOnly = true)
    public OverviewResponse getOverview(UUID userId) {
        log.info("Fetching overview for userId: {}", userId);
        
        List<Object[]> results = summaryRepository.aggregateOverview(userId);
        
        if (results.isEmpty() || results.get(0)[0] == null && results.get(0)[1] == null && results.get(0)[2] == null) {
            return OverviewResponse.builder()
                    .completedPrograms(0)
                    .inProgressPrograms(0)
                    .completedChapters(0)
                    .completedLessons(0)
                    .averageScore(BigDecimal.ZERO)
                    .bestScore(BigDecimal.ZERO)
                    .totalLearningTimeSeconds(0)
                    .totalAttempts(0)
                    .build();
        }

        Object[] row = results.get(0);
        
        long completedPrograms = row[0] != null ? (Long) row[0] : 0;
        long inProgressPrograms = row[1] != null ? (Long) row[1] : 0;
        long completedChapters = row[2] != null ? (Long) row[2] : 0;
        long completedLessons = row[3] != null ? (Long) row[3] : 0;
        long totalExamsTaken = row[4] != null ? (Long) row[4] : 0;
        long passedExams = row[5] != null ? (Long) row[5] : 0;
        long failedExams = row[6] != null ? (Long) row[6] : 0;
        
        Double avgScore = (Double) row[7];
        BigDecimal averageScore = avgScore != null ? BigDecimal.valueOf(avgScore).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        
        BigDecimal bestScore = (BigDecimal) row[8];
        if (bestScore == null) bestScore = BigDecimal.ZERO;
        
        long totalLearningTimeSeconds = row[9] != null ? (Long) row[9] : 0;
        long totalAttempts = row[10] != null ? (Long) row[10] : 0;
        Instant lastActivityAt = (Instant) row[11];

        return OverviewResponse.builder()
                .completedPrograms(completedPrograms)
                .inProgressPrograms(inProgressPrograms)
                .completedChapters(completedChapters)
                .completedLessons(completedLessons)
                .totalExamsTaken(totalExamsTaken)
                .passedExams(passedExams)
                .failedExams(failedExams)
                .averageScore(averageScore)
                .bestScore(bestScore)
                .totalLearningTimeSeconds(totalLearningTimeSeconds)
                .totalAttempts(totalAttempts)
                .lastActivityAt(lastActivityAt)
                .build();
    }
}
