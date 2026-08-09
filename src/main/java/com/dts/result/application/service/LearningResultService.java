package com.dts.result.application.service;

import com.dts.result.application.event.LearningResultEvent;
import com.dts.result.api.exception.ValidationException;
import com.dts.result.application.mapper.LearningResultMapper;
import com.dts.result.domain.entity.LearningResultEntity;
import com.dts.result.domain.repository.LearningResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearningResultService {

    private final LearningResultRepository learningResultRepository;
    private final LearningSummaryService learningSummaryService;
    private final LearningResultMapper resultMapper;

    @Transactional
    public void processLearningResult(LearningResultEvent event) {
        log.info("Processing learning result event for sourceId: {}, sourceType: {}", event.getSourceId(), event.getSourceType());

        // 1. Validate Event (Basic requirement check)
        if (event.getUserId() == null || event.getSourceType() == null || event.getSourceId() == null ||
            event.getTargetType() == null || event.getTargetId() == null || event.getResult() == null) {
            log.error("Validation failed: Missing required fields in LearningResultEvent: {}", event.getEventId());
            throw new ValidationException("Invalid event payload: Missing required fields");
        }
        
        if (event.getProgress() != null && event.getProgress().doubleValue() > 100.0) {
            throw new ValidationException("Progress cannot exceed 100%");
        }

        // 3. Idempotent Check
        boolean exists = learningResultRepository.existsBySourceIdAndSourceType(event.getSourceId(), event.getSourceType());
        if (exists) {
            log.warn("Idempotency check failed: Event with sourceId {} and sourceType {} has already been processed. Skipping.",
                    event.getSourceId(), event.getSourceType());
            return; // ACK
        }

        // 4. Map Event -> LearningResult
        int currentAttempts = learningResultRepository.countByUserIdAndTargetIdAndTargetType(
                event.getUserId(), event.getTargetId(), event.getTargetType());
        int attemptNo = currentAttempts + 1;
        
        LearningResultEntity resultEntity = resultMapper.toEntity(event, attemptNo);

        // 5. Save learning_results
        resultEntity = learningResultRepository.save(resultEntity);
        log.info("Saved LearningResultEntity with id: {}", resultEntity.getId());

        // 6. Trigger Summary Projection
        learningSummaryService.updateProjection(resultEntity, event.getResult());
        
        log.info("Successfully processed LearningResultEvent: {}", event.getEventId());
    }
}
