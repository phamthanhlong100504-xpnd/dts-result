package com.dts.result.application.service;

import com.dts.result.api.exception.ResourceNotFoundException;
import com.dts.result.api.response.ProgressResponse;
import com.dts.result.api.response.ResumeResponse;
import com.dts.result.api.response.SummaryDetailResponse;
import com.dts.result.api.response.SummaryItemResponse;
import com.dts.result.api.response.SummaryStatusResponse;
import com.dts.result.domain.entity.LearningSummaryEntity;
import com.dts.result.domain.repository.LearningSummaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SummaryServiceTest {

    @Mock
    private LearningSummaryRepository summaryRepository;

    @InjectMocks
    private SummaryService summaryService;

    private UUID userId;
    private UUID targetId;
    private LearningSummaryEntity summaryEntity;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        targetId = UUID.randomUUID();
        
        summaryEntity = new LearningSummaryEntity();
        summaryEntity.setUserId(userId);
        summaryEntity.setTargetType("COURSE");
        summaryEntity.setTargetId(targetId);
        summaryEntity.setStatus("COMPLETED");
        summaryEntity.setAttemptCount(1);
        summaryEntity.setBestScore(BigDecimal.valueOf(100));
        summaryEntity.setLatestScore(BigDecimal.valueOf(100));
        summaryEntity.setAverageScore(BigDecimal.valueOf(100));
        summaryEntity.setProgress(BigDecimal.valueOf(100.0));
    }

    @Test
    @DisplayName("getSummaryList - Happy Case")
    void getSummaryList_HappyCase() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<LearningSummaryEntity> page = new PageImpl<>(List.of(summaryEntity));
        when(summaryRepository.findSummaries(userId, "COURSE", "COMPLETED", pageable)).thenReturn(page);

        Page<SummaryItemResponse> response = summaryService.getSummaryList(userId, "COURSE", "COMPLETED", pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("COURSE", response.getContent().get(0).getTargetType());
    }

    @Test
    @DisplayName("getSummaryDetail - Not Found")
    void getSummaryDetail_NotFound() {
        when(summaryRepository.findByUserIdAndTargetTypeAndTargetId(userId, "COURSE", targetId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> summaryService.getSummaryDetail(userId, "COURSE", targetId));
    }

    @Test
    @DisplayName("getSummaryDetail - Happy Case")
    void getSummaryDetail_HappyCase() {
        when(summaryRepository.findByUserIdAndTargetTypeAndTargetId(userId, "COURSE", targetId)).thenReturn(Optional.of(summaryEntity));
        SummaryDetailResponse response = summaryService.getSummaryDetail(userId, "COURSE", targetId);
        assertNotNull(response);
        assertEquals("COURSE", response.getTargetType());
    }

    @Test
    @DisplayName("getStatusStatistics - Happy Case")
    void getStatusStatistics_HappyCase() {
        List<Object[]> results = java.util.Arrays.asList(new Object[]{"COMPLETED", 5L},
                new Object[]{"IN_PROGRESS", 2L},
                new Object[]{"NOT_STARTED", 3L}
        );
        when(summaryRepository.countStatusByUserId(userId)).thenReturn(results);

        SummaryStatusResponse response = summaryService.getStatusStatistics(userId);

        assertEquals(5L, response.getCompleted());
        assertEquals(2L, response.getInProgress());
        assertEquals(3L, response.getNotStarted());
    }

    @Test
    @DisplayName("getProgress - Empty Results")
    void getProgress_EmptyResults() {
        when(summaryRepository.aggregateProgress(userId, "COURSE")).thenReturn(List.of());
        
        ProgressResponse response = summaryService.getProgress(userId, "COURSE");
        
        assertEquals(0L, response.getTotal());
        assertEquals(BigDecimal.ZERO, response.getCompletionRate());
    }

    @Test
    @DisplayName("getProgress - Happy Case")
    void getProgress_HappyCase() {
        List<Object[]> results = new java.util.ArrayList<>();
        results.add(new Object[]{10L, 5L, 3L, 2L, 50.5});
        when(summaryRepository.aggregateProgress(userId, "COURSE")).thenReturn(results);

        ProgressResponse response = summaryService.getProgress(userId, "COURSE");

        assertEquals(10L, response.getTotal());
        assertEquals(5L, response.getCompleted());
        assertEquals(3L, response.getInProgress());
        assertEquals(2L, response.getNotStarted());
        assertEquals(BigDecimal.valueOf(50.00).setScale(2), response.getCompletionRate());
        assertEquals(BigDecimal.valueOf(50.5).setScale(2), response.getAverageProgress());
    }

    @Test
    @DisplayName("getResumeTarget - Happy Case")
    void getResumeTarget_HappyCase() {
        when(summaryRepository.findResumeTarget(userId)).thenReturn(Optional.of(summaryEntity));
        
        ResumeResponse response = summaryService.getResumeTarget(userId);
        
        assertNotNull(response);
        assertEquals("COURSE", response.getTargetType());
    }

    @Test
    @DisplayName("getResumeTarget - Not Found")
    void getResumeTarget_NotFound() {
        when(summaryRepository.findResumeTarget(userId)).thenReturn(Optional.empty());
        assertNull(summaryService.getResumeTarget(userId));
    }
}


