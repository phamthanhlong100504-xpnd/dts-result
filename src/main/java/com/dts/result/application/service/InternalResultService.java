package com.dts.result.application.service;

import com.dts.result.api.response.OverviewResponse;
import com.dts.result.api.response.SummaryItemResponse;
import com.dts.result.domain.entity.LearningSummaryEntity;
import com.dts.result.domain.repository.LearningSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InternalResultService {

    private final OverviewService overviewService;
    private final LearningSummaryRepository summaryRepository;

    @Transactional(readOnly = true)
    public OverviewResponse getUserOverview(UUID userId) {
        log.info("Fetching internal user overview for userId: {}", userId);
        return overviewService.getOverview(userId); // Reusing logic
    }

    @Transactional(readOnly = true)
    public List<SummaryItemResponse> getUserSummaries(UUID userId, String targetType, String status) {
        log.info("Fetching internal user summaries for userId: {}, targetType: {}, status: {}", userId, targetType, status);
        
        List<LearningSummaryEntity> summaries = summaryRepository.findByUserId(userId);
        
        return summaries.stream()
                .filter(s -> targetType == null || targetType.equals(s.getTargetType()))
                .filter(s -> status == null || status.equals(s.getStatus()))
                .map(entity -> SummaryItemResponse.builder()
                        .targetType(entity.getTargetType())
                        .targetId(entity.getTargetId())
                        .status(entity.getStatus())
                        .attemptCount(entity.getAttemptCount())
                        .bestScore(entity.getBestScore())
                        .latestScore(entity.getLatestScore())
                        .averageScore(entity.getAverageScore())
                        .progress(entity.getProgress())
                        .lastActivityAt(entity.getLastActivityAt())
                        .build())
                .collect(Collectors.toList());
    }
}
