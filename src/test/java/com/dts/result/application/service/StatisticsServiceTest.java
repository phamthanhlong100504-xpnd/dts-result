package com.dts.result.application.service;

import com.dts.result.api.response.StatisticsResponse;
import com.dts.result.domain.entity.LearningResultEntity;
import com.dts.result.domain.repository.LearningResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private LearningResultRepository resultRepository;

    @InjectMocks
    private StatisticsService statisticsService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("getStatistics - Date range exceeds 365 days")
    void getStatistics_Exceeds365Days() {
        Instant from = Instant.now().minus(400, ChronoUnit.DAYS);
        Instant to = Instant.now();
        assertThrows(IllegalArgumentException.class, () -> statisticsService.getStatistics(userId, from, to, "DAY"));
    }

    @Test
    @DisplayName("getStatistics - Happy Case")
    void getStatistics_HappyCase() {
        Instant to = Instant.now();
        Instant from = to.minus(10, ChronoUnit.DAYS);
        
        LearningResultEntity e1 = new LearningResultEntity();
        e1.setScore(BigDecimal.valueOf(80.0));
        e1.setDurationSeconds(3600);
        e1.setResult("PASSED");
        e1.setCompletedAt(to);
        
        when(resultRepository.findByUserIdAndCompletedAtBetweenOrderByCompletedAtAsc(eq(userId), any(Instant.class), any(Instant.class)))
            .thenReturn(List.of(e1));

        StatisticsResponse response = statisticsService.getStatistics(userId, from, to, "DAY");
        
        assertNotNull(response);
        assertFalse(response.getScoreTrend().isEmpty());
        assertEquals(BigDecimal.valueOf(80.0).setScale(2, java.math.RoundingMode.HALF_UP), response.getScoreTrend().get(0).getAverageScore());
        assertEquals(3600, response.getStudyTimeTrend().get(0).getDurationSeconds());
        assertEquals(1, response.getAttemptTrend().get(0).getAttempts());
        assertEquals(1, response.getCompletionTrend().get(0).getCompleted());
    }
}
