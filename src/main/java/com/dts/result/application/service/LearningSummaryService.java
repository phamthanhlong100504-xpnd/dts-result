package com.dts.result.application.service;

import com.dts.result.api.response.LearningSummaryResponse;
import com.dts.result.application.exception.ResourceNotFoundException;
import com.dts.result.domain.entity.LearningSummaryEntity;
import com.dts.result.domain.repository.LearningSummaryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LearningSummaryService {

    private final LearningSummaryRepository learningSummaryRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<LearningSummaryResponse> getUserLearningSummaries(UUID callerUserId, String callerRoles, UUID userId) {
        log.info("Fetching user learning summaries for user: {}, caller: {}", userId, callerUserId);
        SecurityUtils.validateOwnershipOrAdmin(callerUserId, callerRoles, userId);
        return learningSummaryRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public LearningSummaryResponse getLearningSummaryDetail(UUID callerUserId, String callerRoles, UUID id) {
        log.info("Fetching learning summary detail for id: {}, caller: {}", id, callerUserId);
        LearningSummaryEntity entity = learningSummaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Learning summary not found with ID: " + id));
        SecurityUtils.validateOwnershipOrAdmin(callerUserId, callerRoles, entity.getUserId());
        return mapToResponse(entity);
    }

    @Transactional
    public LearningSummaryResponse recalculateLearningSummary(UUID callerUserId, String callerRoles, UUID id) {
        log.info("Recalculating learning summary for id: {}, caller: {}", id, callerUserId);
        LearningSummaryEntity entity = learningSummaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Learning summary not found with ID: " + id));
        SecurityUtils.validateOwnershipOrAdmin(callerUserId, callerRoles, entity.getUserId());

        if (entity.getAttemptCount() > 0) {
            entity.setProgress(BigDecimal.valueOf(100));
            entity.setStatus("COMPLETED");
        }
        LearningSummaryEntity saved = learningSummaryRepository.save(entity);
        return mapToResponse(saved);
    }

    private LearningSummaryResponse mapToResponse(LearningSummaryEntity entity) {
        return new LearningSummaryResponse(
                entity.getId(),
                entity.getUserId(),
                entity.getTargetId(),
                entity.getLastResultId(),
                entity.getTargetType(),
                entity.getAttemptCount(),
                entity.getCompletionCount(),
                entity.getBestScore(),
                entity.getLatestScore(),
                entity.getAverageScore(),
                entity.getProgress(),
                entity.getTotalDurationSeconds(),
                entity.getStatus(),
                entity.getLastActivityAt(),
                entity.getCompletedAt(),
                parseJson(entity.getSummarySnapshot()),
                parseJson(entity.getMetadata()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
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
