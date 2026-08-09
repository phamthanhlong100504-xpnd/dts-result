package com.dts.result.application.service;

import com.dts.result.api.exception.ResourceNotFoundException;
import com.dts.result.api.response.HistoryDetailResponse;
import com.dts.result.api.response.HistoryItemResponse;
import com.dts.result.api.response.RecentActivityResponse;
import com.dts.result.domain.entity.LearningResultEntity;
import com.dts.result.domain.repository.LearningResultRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HistoryService {

    private final LearningResultRepository learningResultRepository;

    @Transactional(readOnly = true)
    public Page<HistoryItemResponse> getHistoryList(UUID userId, String targetType, UUID targetId, String result, Instant from, Instant to, Pageable pageable) {
        log.info("Fetching history for userId: {}, targetType: {}, targetId: {}", userId, targetType, targetId);

        Specification<LearningResultEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("userId"), userId));
            
            if (targetType != null) {
                predicates.add(cb.equal(root.get("targetType"), targetType));
            }
            if (targetId != null) {
                predicates.add(cb.equal(root.get("targetId"), targetId));
            }
            if (result != null) {
                predicates.add(cb.equal(root.get("result"), result));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("completedAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("completedAt"), to));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<LearningResultEntity> entities = learningResultRepository.findAll(spec, pageable);
        
        return entities.map(entity -> HistoryItemResponse.builder()
                .id(entity.getId())
                .targetType(entity.getTargetType())
                .targetId(entity.getTargetId())
                .attemptNo(entity.getAttemptNo())
                .result(entity.getResult())
                .score(entity.getScore())
                .maxScore(entity.getMaxScore())
                .progress(entity.getProgress())
                .durationSeconds(entity.getDurationSeconds())
                .completedAt(entity.getCompletedAt())
                .build());
    }

    @Transactional(readOnly = true)
    public HistoryDetailResponse getHistoryDetail(UUID userId, UUID resultId) {
        log.info("Fetching history detail for userId: {}, resultId: {}", userId, resultId);
        
        LearningResultEntity entity = learningResultRepository.findById(resultId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning result not found"));
                
        if (!entity.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Learning result not found"); // Returning 404 instead of 403 for security reasons (don't reveal existence)
        }

        return HistoryDetailResponse.builder()
                .id(entity.getId())
                .targetType(entity.getTargetType())
                .targetId(entity.getTargetId())
                .attemptNo(entity.getAttemptNo())
                .result(entity.getResult())
                .score(entity.getScore())
                .maxScore(entity.getMaxScore())
                .progress(entity.getProgress())
                .durationSeconds(entity.getDurationSeconds())
                .startedAt(entity.getStartedAt())
                .completedAt(entity.getCompletedAt())
                .resultSnapshot(entity.getResultSnapshot())
                .metadata(entity.getMetadata())
                .build();
    }

    @Transactional(readOnly = true)
    public List<RecentActivityResponse> getRecentActivities(UUID userId, int limit) {
        log.info("Fetching recent activities for userId: {}, limit: {}", userId, limit);
        
        List<LearningResultEntity> recentResults = learningResultRepository.findTop10ByUserIdOrderByCompletedAtDesc(userId);
        
        if (recentResults.size() > limit) {
            recentResults = recentResults.subList(0, limit);
        }

        return recentResults.stream()
                .map(entity -> RecentActivityResponse.builder()
                        .targetType(entity.getTargetType())
                        .targetId(entity.getTargetId())
                        .result(entity.getResult())
                        .score(entity.getScore())
                        .completedAt(entity.getCompletedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
