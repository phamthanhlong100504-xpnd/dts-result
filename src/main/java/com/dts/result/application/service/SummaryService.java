package com.dts.result.application.service;

import com.dts.result.api.exception.ResourceNotFoundException;
import com.dts.result.api.response.ProgressResponse;
import com.dts.result.api.response.ResumeResponse;
import com.dts.result.api.response.SummaryDetailResponse;
import com.dts.result.api.response.SummaryItemResponse;
import com.dts.result.api.response.SummaryStatusResponse;
import com.dts.result.domain.entity.LearningSummaryEntity;
import com.dts.result.domain.repository.LearningSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SummaryService {

    private final LearningSummaryRepository summaryRepository;

    @Transactional(readOnly = true)
    public Page<SummaryItemResponse> getSummaryList(UUID userId, String targetType, String status, Pageable pageable) {
        log.info("Fetching summaries for userId: {}, targetType: {}, status: {}", userId, targetType, status);
        Page<LearningSummaryEntity> entities = summaryRepository.findSummaries(userId, targetType, status, pageable);
        
        return entities.map(entity -> SummaryItemResponse.builder()
                .targetType(entity.getTargetType())
                .targetId(entity.getTargetId())
                .status(entity.getStatus())
                .attemptCount(entity.getAttemptCount())
                .bestScore(entity.getBestScore())
                .latestScore(entity.getLatestScore())
                .averageScore(entity.getAverageScore())
                .progress(entity.getProgress())
                .lastActivityAt(entity.getLastActivityAt())
                .build());
    }

    @Transactional(readOnly = true)
    public SummaryDetailResponse getSummaryDetail(UUID userId, String targetType, UUID targetId) {
        log.info("Fetching summary detail for userId: {}, targetType: {}, targetId: {}", userId, targetType, targetId);
        
        LearningSummaryEntity entity = summaryRepository.findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning summary not found"));

        return SummaryDetailResponse.builder()
                .targetType(entity.getTargetType())
                .targetId(entity.getTargetId())
                .status(entity.getStatus())
                .attemptCount(entity.getAttemptCount())
                .completionCount(entity.getCompletionCount())
                .bestScore(entity.getBestScore())
                .latestScore(entity.getLatestScore())
                .averageScore(entity.getAverageScore())
                .progress(entity.getProgress())
                .totalDurationSeconds(entity.getTotalDurationSeconds())
                .lastActivityAt(entity.getLastActivityAt())
                .completedAt(entity.getCompletedAt())
                .summarySnapshot(entity.getSummarySnapshot())
                .metadata(entity.getMetadata())
                .build();
    }

    @Transactional(readOnly = true)
    public SummaryStatusResponse getStatusStatistics(UUID userId) {
        log.info("Fetching status statistics for userId: {}", userId);
        
        List<Object[]> results = summaryRepository.countStatusByUserId(userId);
        
        long completed = 0;
        long inProgress = 0;
        long notStarted = 0;
        
        for (Object[] result : results) {
            String status = (String) result[0];
            long count = (Long) result[1];
            
            if ("COMPLETED".equals(status)) {
                completed = count;
            } else if ("IN_PROGRESS".equals(status)) {
                inProgress = count;
            } else if ("NOT_STARTED".equals(status)) {
                notStarted = count;
            }
        }
        
        return SummaryStatusResponse.builder()
                .completed(completed)
                .inProgress(inProgress)
                .notStarted(notStarted)
                .build();
    }

    @Transactional(readOnly = true)
    public ProgressResponse getProgress(UUID userId, String targetType) {
        log.info("Fetching progress for userId: {}, targetType: {}", userId, targetType);
        
        List<Object[]> results = summaryRepository.aggregateProgress(userId, targetType);
        
        if (results.isEmpty() || results.get(0)[0] == null || (Long) results.get(0)[0] == 0) {
            return ProgressResponse.builder()
                    .targetType(targetType)
                    .total(0).completed(0).inProgress(0).notStarted(0)
                    .completionRate(BigDecimal.ZERO)
                    .averageProgress(BigDecimal.ZERO)
                    .build();
        }
        
        Object[] row = results.get(0);
        long total = (Long) row[0];
        long completed = row[1] != null ? (Long) row[1] : 0;
        long inProgress = row[2] != null ? (Long) row[2] : 0;
        long notStarted = row[3] != null ? (Long) row[3] : 0;
        Double avgProg = (Double) row[4];
        
        BigDecimal completionRate = total > 0 
                ? BigDecimal.valueOf(completed).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
                
        BigDecimal averageProgress = avgProg != null 
                ? BigDecimal.valueOf(avgProg).setScale(2, RoundingMode.HALF_UP) 
                : BigDecimal.ZERO;
                
        return ProgressResponse.builder()
                .targetType(targetType)
                .total(total)
                .completed(completed)
                .inProgress(inProgress)
                .notStarted(notStarted)
                .completionRate(completionRate)
                .averageProgress(averageProgress)
                .build();
    }

    @Transactional(readOnly = true)
    public ResumeResponse getResumeTarget(UUID userId) {
        log.info("Fetching resume target for userId: {}", userId);
        
        return summaryRepository.findResumeTarget(userId)
                .map(entity -> ResumeResponse.builder()
                        .targetType(entity.getTargetType())
                        .targetId(entity.getTargetId())
                        .progress(entity.getProgress())
                        .lastActivityAt(entity.getLastActivityAt())
                        .build())
                .orElse(null);
    }
}
