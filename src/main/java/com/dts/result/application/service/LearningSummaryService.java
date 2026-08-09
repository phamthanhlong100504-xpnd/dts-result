package com.dts.result.application.service;

import com.dts.result.application.mapper.LearningSummaryMapper;
import com.dts.result.domain.entity.LearningResultEntity;
import com.dts.result.domain.entity.LearningSummaryEntity;
import com.dts.result.domain.repository.LearningSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearningSummaryService {

    private final LearningSummaryRepository learningSummaryRepository;
    private final LearningSummaryMapper summaryMapper;

    public void updateProjection(LearningResultEntity resultEntity, String eventResult) {
        log.info("Updating projection for userId: {}, targetType: {}, targetId: {}",
                resultEntity.getUserId(), resultEntity.getTargetType(), resultEntity.getTargetId());

        Optional<LearningSummaryEntity> summaryOpt = learningSummaryRepository
                .findByUserIdAndTargetTypeAndTargetId(resultEntity.getUserId(), resultEntity.getTargetType(), resultEntity.getTargetId());

        LearningSummaryEntity summary;
        BigDecimal eventScore = resultEntity.getScore() != null ? resultEntity.getScore() : BigDecimal.ZERO;
        boolean isSuccessResult = "PASSED".equals(eventResult) || "COMPLETED".equals(eventResult) || "SUBMITTED".equals(eventResult);

        if (summaryOpt.isEmpty()) {
            summary = LearningSummaryEntity.builder()
                    .userId(resultEntity.getUserId())
                    .targetId(resultEntity.getTargetId())
                    .lastResultId(resultEntity.getId())
                    .targetType(resultEntity.getTargetType())
                    .attemptCount(1)
                    .completionCount(isSuccessResult ? 1 : 0)
                    .bestScore(eventScore)
                    .latestScore(eventScore)
                    .averageScore(eventScore)
                    .progress(resultEntity.getProgress())
                    .totalDurationSeconds(resultEntity.getDurationSeconds())
                    .status(summaryMapper.deriveStatus(resultEntity.getProgress(), eventResult))
                    .lastActivityAt(resultEntity.getCompletedAt() != null ? resultEntity.getCompletedAt() : Instant.now())
                    .completedAt(isSuccessResult ? resultEntity.getCompletedAt() : null)
                    .summarySnapshot(summaryMapper.deriveSnapshot(eventResult, eventScore, resultEntity.getMaxScore(), resultEntity.getResultSnapshot()))
                    .metadata(resultEntity.getMetadata())
                    .build();
        } else {
            summary = summaryOpt.get();
            summary.setLastResultId(resultEntity.getId());
            
            int oldAttemptCount = summary.getAttemptCount();
            int newAttemptCount = oldAttemptCount + 1;
            summary.setAttemptCount(newAttemptCount);
            
            if (isSuccessResult) {
                summary.setCompletionCount(summary.getCompletionCount() + 1);
            }
            
            summary.setLatestScore(eventScore);
            
            if (summary.getBestScore() == null || eventScore.compareTo(summary.getBestScore()) > 0) {
                summary.setBestScore(eventScore);
            }
            
            // Recalculate average score incrementally
            if (summary.getAverageScore() != null) {
                BigDecimal oldTotal = summary.getAverageScore().multiply(BigDecimal.valueOf(oldAttemptCount));
                BigDecimal newTotal = oldTotal.add(eventScore);
                summary.setAverageScore(newTotal.divide(BigDecimal.valueOf(newAttemptCount), 2, RoundingMode.HALF_UP));
            } else {
                summary.setAverageScore(eventScore);
            }
            
            summary.setTotalDurationSeconds(summary.getTotalDurationSeconds() + resultEntity.getDurationSeconds());
            
            Instant completedAt = resultEntity.getCompletedAt() != null ? resultEntity.getCompletedAt() : Instant.now();
            if (summary.getLastActivityAt() == null || completedAt.isAfter(summary.getLastActivityAt())) {
                summary.setLastActivityAt(completedAt);
            }
            
            if (summary.getProgress() == null || resultEntity.getProgress().compareTo(summary.getProgress()) > 0) {
                summary.setProgress(resultEntity.getProgress());
            }
            
            String newStatus = summaryMapper.deriveStatus(summary.getProgress(), eventResult);
            if (!"COMPLETED".equals(summary.getStatus()) && "COMPLETED".equals(newStatus)) {
                summary.setStatus("COMPLETED");
                if (summary.getCompletedAt() == null) {
                    summary.setCompletedAt(resultEntity.getCompletedAt());
                }
            } else {
                summary.setStatus(newStatus);
            }
            
            summary.setSummarySnapshot(summaryMapper.deriveSnapshot(eventResult, eventScore, resultEntity.getMaxScore(), resultEntity.getResultSnapshot()));
        }

        learningSummaryRepository.save(summary);
        
        log.info("Projection updated - userId: {}, targetType: {}, targetId: {}, attemptCount: {}, bestScore: {}, latestScore: {}, status: {}",
                summary.getUserId(), summary.getTargetType(), summary.getTargetId(),
                summary.getAttemptCount(), summary.getBestScore(), summary.getLatestScore(), summary.getStatus());
    }
}
