package com.dts.result.application.service;

import com.dts.result.api.exception.ValidationException;
import com.dts.result.application.event.LearningResultEvent;
import com.dts.result.application.mapper.LearningResultMapper;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LearningResultServiceTest {

    @Mock private LearningResultRepository learningResultRepository;
    @Mock private LearningSummaryService learningSummaryService;
    @Mock private LearningResultMapper resultMapper;

    @InjectMocks private LearningResultService learningResultService;

    private LearningResultEvent validEvent;

    @BeforeEach
    void setUp() {
        validEvent = new LearningResultEvent();
        validEvent.setEventId(UUID.randomUUID());
        validEvent.setUserId(UUID.randomUUID());
        validEvent.setSourceType("QUIZ");
        validEvent.setSourceId(UUID.randomUUID());
        validEvent.setTargetType("COURSE");
        validEvent.setTargetId(UUID.randomUUID());
        validEvent.setResult("PASS");
        validEvent.setProgress(100.0);
    }

    @Test
    @DisplayName("processLearningResult - Validation Failed (Missing userId)")
    void processLearningResult_ValidationFailed_MissingUserId() {
        validEvent.setUserId(null);
        assertThrows(ValidationException.class, () -> learningResultService.processLearningResult(validEvent));
    }

    @Test
    @DisplayName("processLearningResult - Validation Failed (Progress > 100)")
    void processLearningResult_ValidationFailed_ProgressExceed() {
        validEvent.setProgress(101.0);
        assertThrows(ValidationException.class, () -> learningResultService.processLearningResult(validEvent));
    }

    @Test
    @DisplayName("processLearningResult - Idempotency Check Failed (Already processed)")
    void processLearningResult_IdempotencyFailed_AlreadyProcessed() {
        when(learningResultRepository.existsBySourceIdAndSourceType(validEvent.getSourceId(), validEvent.getSourceType())).thenReturn(true);
        learningResultService.processLearningResult(validEvent);
        verify(learningResultRepository, never()).save(any());
        verify(learningSummaryService, never()).updateProjection(any(), anyString());
    }

    @Test
    @DisplayName("processLearningResult - Happy Case")
    void processLearningResult_HappyCase() {
        when(learningResultRepository.existsBySourceIdAndSourceType(validEvent.getSourceId(), validEvent.getSourceType())).thenReturn(false);
        when(learningResultRepository.countByUserIdAndTargetIdAndTargetType(validEvent.getUserId(), validEvent.getTargetId(), validEvent.getTargetType())).thenReturn(0);
        
        LearningResultEntity entity = new LearningResultEntity();
        when(resultMapper.toEntity(validEvent, 1)).thenReturn(entity);
        when(learningResultRepository.save(entity)).thenReturn(entity);

        learningResultService.processLearningResult(validEvent);

        verify(learningResultRepository, times(1)).save(entity);
        verify(learningSummaryService, times(1)).updateProjection(entity, "PASS");
    }
}

