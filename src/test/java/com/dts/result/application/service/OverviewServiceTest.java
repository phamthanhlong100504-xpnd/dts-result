package com.dts.result.application.service;

import com.dts.result.api.response.OverviewResponse;
import com.dts.result.domain.repository.LearningSummaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OverviewServiceTest {

    @Mock
    private LearningSummaryRepository summaryRepository;

    @InjectMocks
    private OverviewService overviewService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("getOverview - Empty Results")
    void getOverview_EmptyResults() {
        when(summaryRepository.aggregateOverview(userId)).thenReturn(new ArrayList<>());
        
        OverviewResponse response = overviewService.getOverview(userId);
        
        assertEquals(0, response.getCompletedPrograms());
        assertEquals(0, response.getInProgressPrograms());
        assertEquals(0, response.getCompletedChapters());
        assertEquals(0, response.getCompletedLessons());
        assertEquals(BigDecimal.ZERO, response.getAverageScore());
        assertEquals(BigDecimal.ZERO, response.getBestScore());
        assertEquals(0, response.getTotalLearningTimeSeconds());
        assertEquals(0, response.getTotalAttempts());
    }

    @Test
    @DisplayName("getOverview - Happy Case")
    void getOverview_HappyCase() {
        Instant now = Instant.now();
        List<Object[]> results = new ArrayList<>();
        results.add(new Object[]{
                1L, // completedPrograms
                2L, // inProgressPrograms
                3L, // completedChapters
                4L, // completedLessons
                5L, // totalExamsTaken
                3L, // passedExams
                2L, // failedExams
                80.5, // averageScore
                BigDecimal.valueOf(100), // bestScore
                3600L, // totalLearningTimeSeconds
                10L, // totalAttempts
                now // lastActivityAt
        });
        when(summaryRepository.aggregateOverview(userId)).thenReturn(results);

        OverviewResponse response = overviewService.getOverview(userId);

        assertNotNull(response);
        assertEquals(1L, response.getCompletedPrograms());
        assertEquals(2L, response.getInProgressPrograms());
        assertEquals(3L, response.getCompletedChapters());
        assertEquals(4L, response.getCompletedLessons());
        assertEquals(5L, response.getTotalExamsTaken());
        assertEquals(3L, response.getPassedExams());
        assertEquals(2L, response.getFailedExams());
        assertEquals(0, BigDecimal.valueOf(80.50).setScale(2).compareTo(response.getAverageScore()));
        assertEquals(BigDecimal.valueOf(100), response.getBestScore());
        assertEquals(3600L, response.getTotalLearningTimeSeconds());
        assertEquals(10L, response.getTotalAttempts());
        assertEquals(now, response.getLastActivityAt());
    }
}
