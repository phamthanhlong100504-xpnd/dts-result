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
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearningResultService {
    private final LearningResultRepository learningResultRepository;
    private final LearningSummaryService learningSummaryService;
    private final LearningResultMapper resultMapper;
    private final ResultWriteLock writeLock;
    private final ExamHistoryProjection examProjection;

    @Transactional
    public void processLearningResult(LearningResultEvent event) {
        if (event == null || event.getUserId() == null || event.getSourceType() == null
                || event.getSourceId() == null || event.getTargetType() == null
                || event.getTargetId() == null || event.getResult() == null)
            throw new ValidationException("Invalid event payload: Missing required fields");
        if (event.getProgress() != null && (!Double.isFinite(event.getProgress())
                || event.getProgress() < 0 || event.getProgress() > 100))
            throw new ValidationException("Progress must be between 0 and 100");
        boolean exam = "EXAM".equals(event.getTargetType());
        if (exam) validateExam(event);
        writeLock.acquire(event.getSourceId(),event.getSourceType(),event.getUserId());
        var previous = learningResultRepository.findBySourceIdAndSourceType(event.getSourceId(),event.getSourceType());
        if (previous.isPresent()) {
            var old = previous.get();
            if (!old.getUserId().equals(event.getUserId()))
                throw new ValidationException("The source result belongs to another user");
            // A v2 source backfill can enrich a v1 record, never overwrite another v2 snapshot.
            if (exam && event.getEventVersion() != null && event.getEventVersion() >= 2
                    && (old.getEventVersion() == null || old.getEventVersion() < 2)) {
                UUID oldTarget=old.getTargetId();
                var enriched=resultMapper.toEntity(event,event.getAttemptNo());
                enriched.setId(old.getId());enriched.setCreatedAt(old.getCreatedAt());
                learningResultRepository.saveAndFlush(enriched);
                examProjection.rebuild(event.getUserId(),event.getTargetId());
                if (!oldTarget.equals(event.getTargetId())) examProjection.rebuild(event.getUserId(),oldTarget);
            }
            return;
        }
        int attempt;
        if (event.getAttemptNo() != null) {
            if (event.getAttemptNo() < 1) throw new ValidationException("attemptNo must be positive");
            attempt=event.getAttemptNo();
        } else {
            // Only legacy non-exam producers lack source attempt numbers.
            attempt=learningResultRepository.countByUserIdAndTargetIdAndTargetType(
                    event.getUserId(),event.getTargetId(),event.getTargetType())+1;
        }
        LearningResultEntity entity=learningResultRepository.saveAndFlush(resultMapper.toEntity(event,attempt));
        if (exam) examProjection.rebuild(event.getUserId(),event.getTargetId());
        else learningSummaryService.updateProjection(entity,event.getResult());
        log.info("Stored result sourceType={}, sourceId={}, attemptNo={}",event.getSourceType(),event.getSourceId(),attempt);
    }

    private void validateExam(LearningResultEvent e) {
        // SUBMITTED legacy messages contain unreleased grades: reject rather than store/expose them.
        if (!Set.of("PASSED","FAILED").contains(e.getResult()) || e.getCompletedAt() == null
                || e.getStartedAt() == null || e.getCompletedAt().isBefore(e.getStartedAt())
                || e.getAttemptNo() == null || e.getAttemptNo() < 1)
            throw new ValidationException("An exam event must be completed, released and carry its source attemptNo");
        if (!Set.of("EXAM_SESSION","PRACTICE_SESSION").contains(e.getSourceType()))
            throw new ValidationException("Unsupported exam sourceType");
        if (e.getDurationSeconds() != null && e.getDurationSeconds() < 0)
            throw new ValidationException("durationSeconds must not be negative");
        if (e.getEventVersion()!=null && (e.getEventVersion()<1 || e.getEventVersion()>2))
            throw new ValidationException("Unsupported exam eventVersion");
        if (e.getEventVersion() != null && e.getEventVersion() >= 2) {
            if (!"LEARNING_RESULT_CREATED".equals(e.getEventType()) || e.getEventId() == null
                    || !Boolean.TRUE.equals(e.getResultAvailable()) || e.getActivityType() == null
                    || !Set.of("RANDOM_EXAM","PRESET_EXAM","OFFICIAL_EXAM").contains(e.getActivityType())
                    || e.getLicenseClass() == null || e.getLicenseClass().isBlank()
                    || e.getLicenseClass().length() > 10 || e.getDisplayTitle() != null && e.getDisplayTitle().length() > 200
                    || e.getQuestionCount() == null || e.getCorrectCount() == null
                    || e.getQuestionCount() < 1 || e.getCorrectCount() < 0 || e.getCorrectCount() > e.getQuestionCount()
                    || e.getDurationSeconds() == null || e.getScore() == null || e.getMaxScore() == null
                    || e.getMaxScore().signum() <= 0 || e.getScore().signum() < 0 || e.getScore().compareTo(e.getMaxScore()) > 0
                    || "OFFICIAL_EXAM".equals(e.getActivityType()) != "EXAM_SESSION".equals(e.getSourceType()))
                throw new ValidationException("Invalid v2 exam snapshot");
        } else {
            e.setResultAvailable(true); // v1 PASSED/FAILED was emitted by an immediate-release producer.
        }
    }
}
