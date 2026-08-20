package com.dts.result.application.service;

import com.dts.result.application.mapper.LearningSummaryMapper;
import com.dts.result.domain.entity.LearningResultEntity;
import com.dts.result.domain.entity.LearningSummaryEntity;
import com.dts.result.domain.repository.LearningSummaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LearningSummaryServiceTest {

    @Mock
    private LearningSummaryRepository summaryRepository;

    @Mock
    private LearningSummaryMapper summaryMapper;

    @InjectMocks
    private LearningSummaryService summaryService;

    @Captor
    private ArgumentCaptor<LearningSummaryEntity> summaryCaptor;

    private LearningResultEntity resultEntity;
    private UUID userId;
    private UUID targetId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        targetId = UUID.randomUUID();

        resultEntity = new LearningResultEntity();
        resultEntity.setId(UUID.randomUUID());
        resultEntity.setUserId(userId);
        resultEntity.setTargetType("LESSON");
        resultEntity.setTargetId(targetId);
        resultEntity.setScore(BigDecimal.valueOf(80));
        resultEntity.setProgress(BigDecimal.valueOf(50));
        resultEntity.setDurationSeconds(120);
        resultEntity.setCompletedAt(Instant.now());
    }

    @Test
    @DisplayName("updateProjection - Create new summary")
    void updateProjection_CreateNew() {
        when(summaryRepository.findByUserIdAndTargetTypeAndTargetId(userId, "LESSON", targetId))
                .thenReturn(Optional.empty());
        when(summaryMapper.deriveStatus(any(), eq("PASSED"))).thenReturn("COMPLETED");

        summaryService.updateProjection(resultEntity, "PASSED");

        verify(summaryRepository).save(summaryCaptor.capture());
        LearningSummaryEntity saved = summaryCaptor.getValue();
        
        assertEquals(userId, saved.getUserId());
        assertEquals("LESSON", saved.getTargetType());
        assertEquals(1, saved.getAttemptCount());
        assertEquals(1, saved.getCompletionCount());
        assertEquals(BigDecimal.valueOf(80), saved.getBestScore());
        assertEquals(BigDecimal.valueOf(80), saved.getLatestScore());
        assertEquals(BigDecimal.valueOf(80), saved.getAverageScore());
        assertEquals("COMPLETED", saved.getStatus());
    }

    @Test
    @DisplayName("updateProjection - Update existing summary")
    void updateProjection_UpdateExisting() {
        LearningSummaryEntity existing = new LearningSummaryEntity();
        existing.setUserId(userId);
        existing.setTargetType("LESSON");
        existing.setTargetId(targetId);
        existing.setAttemptCount(1);
        existing.setCompletionCount(0);
        existing.setBestScore(BigDecimal.valueOf(70));
        existing.setLatestScore(BigDecimal.valueOf(70));
        existing.setAverageScore(BigDecimal.valueOf(70));
        existing.setProgress(BigDecimal.valueOf(20));
        existing.setTotalDurationSeconds(60);
        existing.setStatus("IN_PROGRESS");
        
        when(summaryRepository.findByUserIdAndTargetTypeAndTargetId(userId, "LESSON", targetId))
                .thenReturn(Optional.of(existing));
        when(summaryMapper.deriveStatus(any(), eq("PASSED"))).thenReturn("COMPLETED");

        summaryService.updateProjection(resultEntity, "PASSED");

        verify(summaryRepository).save(summaryCaptor.capture());
        LearningSummaryEntity saved = summaryCaptor.getValue();
        
        assertEquals(2, saved.getAttemptCount());
        assertEquals(1, saved.getCompletionCount());
        assertEquals(BigDecimal.valueOf(80), saved.getBestScore());
        assertEquals(BigDecimal.valueOf(80), saved.getLatestScore());
        // (70 * 1 + 80) / 2 = 75
        assertEquals(0, BigDecimal.valueOf(75.00).compareTo(saved.getAverageScore()));
        assertEquals(BigDecimal.valueOf(50), saved.getProgress());
        assertEquals(180, saved.getTotalDurationSeconds());
        assertEquals("COMPLETED", saved.getStatus());
    }

    @Test
    @DisplayName("updateProjection - No Score")
    void updateProjection_NoScore() {
        resultEntity.setScore(null);
        when(summaryRepository.findByUserIdAndTargetTypeAndTargetId(userId, "LESSON", targetId))
                .thenReturn(Optional.empty());
        when(summaryMapper.deriveStatus(any(), eq("IN_PROGRESS"))).thenReturn("IN_PROGRESS");

        summaryService.updateProjection(resultEntity, "IN_PROGRESS");

        verify(summaryRepository).save(summaryCaptor.capture());
        LearningSummaryEntity saved = summaryCaptor.getValue();
        
        assertNull(saved.getBestScore());
        assertNull(saved.getLatestScore());
        assertNull(saved.getAverageScore());
        assertEquals(0, saved.getCompletionCount());
    }
}

