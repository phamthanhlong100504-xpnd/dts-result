package com.dts.result.application.service;

import com.dts.result.api.form.RecordLearningResultRequest;
import com.dts.result.api.response.LearningResultResponse;
import com.dts.result.api.response.PageResponse;
import com.dts.result.application.exception.ResourceNotFoundException;
import com.dts.result.domain.entity.LearningResultEntity;
import com.dts.result.domain.entity.LearningSummaryEntity;
import com.dts.result.domain.repository.LearningResultRepository;
import com.dts.result.domain.repository.LearningSummaryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LearningResultService {

    private final LearningResultRepository learningResultRepository;
    private final LearningSummaryRepository learningSummaryRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public LearningResultResponse recordLearningResult(UUID userId, RecordLearningResultRequest request) {
        log.info("Recording learning result for user: {}, source: {}", userId, request.sourceId());

        // Idempotency check on (sourceId, sourceType)
        Optional<LearningResultEntity> existing = learningResultRepository.findBySourceIdAndSourceType(request.sourceId(), request.sourceType());
        if (existing.isPresent()) {
            return mapToResponse(existing.get());
        }

        int maxAttempt = learningResultRepository.findMaxAttemptNo(userId, request.targetType(), request.targetId());
        int attemptNo = maxAttempt + 1;

        LearningResultEntity entity = LearningResultEntity.builder()
                .userId(userId)
                .sourceId(request.sourceId())
                .targetId(request.targetId())
                .sourceType(request.sourceType())
                .targetType(request.targetType())
                .attemptNo(attemptNo)
                .score(request.score())
                .maxScore(request.maxScore())
                .progress(request.progress())
                .durationSeconds(request.durationSeconds())
                .result(request.result())
                .startedAt(request.startedAt())
                .completedAt(request.completedAt())
                .resultSnapshot(toJson(request.resultSnapshot()))
                .metadata(toJson(request.metadata()))
                .build();

        LearningResultEntity saved = learningResultRepository.save(entity);

        // Update Learning Summary Rollup
        updateLearningSummary(userId, saved);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public LearningResultResponse getLearningResultDetail(UUID callerUserId, String callerRoles, UUID id) {
        LearningResultEntity entity = learningResultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Learning result not found with ID: " + id));
        SecurityUtils.validateOwnershipOrAdmin(callerUserId, callerRoles, entity.getUserId());
        return mapToResponse(entity);
    }

    @Transactional(readOnly = true)
    public PageResponse<LearningResultResponse> listLearningResults(UUID userId, int page, int size) {
        int pageIndex = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "created_at"));

        Page<LearningResultEntity> resultPage = learningResultRepository.findByUserId(userId, pageable);
        List<LearningResultResponse> items = resultPage.getContent().stream().map(this::mapToResponse).toList();

        return new PageResponse<>(items, page, size, resultPage.getTotalElements(), resultPage.getTotalPages());
    }

    private void updateLearningSummary(UUID userId, LearningResultEntity result) {
        Optional<LearningSummaryEntity> summaryOpt = learningSummaryRepository.findByUserIdAndTargetTypeAndTargetId(
                userId, result.getTargetType(), result.getTargetId());

        LearningSummaryEntity summary = summaryOpt.orElseGet(() -> LearningSummaryEntity.builder()
                .userId(userId)
                .targetId(result.getTargetId())
                .targetType(result.getTargetType())
                .attemptCount(0)
                .completionCount(0)
                .progress(BigDecimal.ZERO)
                .totalDurationSeconds(0)
                .status("NOT_STARTED")
                .build());

        summary.setLastResultId(result.getId());
        summary.setAttemptCount(summary.getAttemptCount() + 1);
        if ("PASSED".equalsIgnoreCase(result.getResult()) || "COMPLETED".equalsIgnoreCase(result.getResult())) {
            summary.setCompletionCount(summary.getCompletionCount() + 1);
            summary.setStatus("COMPLETED");
            summary.setCompletedAt(result.getCompletedAt() != null ? result.getCompletedAt() : OffsetDateTime.now());
        } else {
            summary.setStatus("IN_PROGRESS");
        }

        if (result.getScore() != null) {
            summary.setLatestScore(result.getScore());
            if (summary.getBestScore() == null || result.getScore().compareTo(summary.getBestScore()) > 0) {
                summary.setBestScore(result.getScore());
            }
            if (summary.getAverageScore() == null) {
                summary.setAverageScore(result.getScore());
            } else {
                BigDecimal totalScoreSum = summary.getAverageScore().multiply(BigDecimal.valueOf(summary.getAttemptCount() - 1)).add(result.getScore());
                summary.setAverageScore(totalScoreSum.divide(BigDecimal.valueOf(summary.getAttemptCount()), 2, RoundingMode.HALF_UP));
            }
        }

        summary.setProgress(result.getProgress());
        summary.setTotalDurationSeconds(summary.getTotalDurationSeconds() + result.getDurationSeconds());
        summary.setLastActivityAt(result.getStartedAt());

        learningSummaryRepository.save(summary);
    }

    private LearningResultResponse mapToResponse(LearningResultEntity entity) {
        return new LearningResultResponse(
                entity.getId(),
                entity.getUserId(),
                entity.getSourceId(),
                entity.getTargetId(),
                entity.getSourceType(),
                entity.getTargetType(),
                entity.getAttemptNo(),
                entity.getScore(),
                entity.getMaxScore(),
                entity.getProgress(),
                entity.getDurationSeconds(),
                entity.getResult(),
                entity.getStartedAt(),
                entity.getCompletedAt(),
                parseJson(entity.getResultSnapshot()),
                parseJson(entity.getMetadata()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private Object parseJson(String str) {
        if (str == null || str.isBlank()) return null;
        try {
            return objectMapper.readValue(str, Object.class);
        } catch (Exception e) {
            return str;
        }
    }
}
