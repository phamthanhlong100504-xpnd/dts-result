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

    private static final java.util.Set<String> ACTIVITIES = java.util.Set.of("PRESET_EXAM","RANDOM_EXAM","OFFICIAL_EXAM");
    private static final java.util.Set<String> LICENSES = java.util.Set.of("A1","A","B1","B","C1","C","D1","D2","D","BE","C1E","CE","D1E","D2E","DE","A2","B2","E","F");
    private static final java.util.Set<String> SORTS = java.util.Set.of("completedAt","startedAt","score","id");
    private static final java.util.Set<String> SAFE_SNAPSHOT = java.util.Set.of("totalQuestions","correctCount","questionCount","examId","examVersionId","licenseClass","presetExamSetId","bankVersion");
    private static final java.util.Set<String> SAFE_METADATA = java.util.Set.of("submissionReason","presetExamSetId","bankVersion","examVersionId","mode","legacySnapshot");

    @Transactional(readOnly = true)
    public Page<HistoryItemResponse> getHistoryList(UUID userId, String targetType, UUID targetId, String result, Instant from, Instant to, Pageable pageable) {
        return getHistoryList(userId,targetType,targetId,result,null,null,from,to,pageable);
    }

    @Transactional(readOnly = true)
    public Page<HistoryItemResponse> getHistoryList(UUID userId, String targetType, UUID targetId, String result,
            String activityType, String licenseClass, Instant from, Instant to, Pageable pageable) {
        if (from != null && to != null && from.isAfter(to)) throw invalid("from must not be after to");
        if (activityType != null && !ACTIVITIES.contains(activityType)) throw invalid("Invalid activityType");
        if (licenseClass != null && !LICENSES.contains(licenseClass)) throw invalid("Invalid licenseClass");
        if (targetType != null && !java.util.Set.of("EXAM","PRACTICE_SET","CHAPTER","LESSON","LEARNING_PROGRAM").contains(targetType)) throw invalid("Invalid targetType");
        if (result != null && !java.util.Set.of("PASSED","FAILED","COMPLETED","ABANDONED","EXPIRED","SUBMITTED").contains(result)) throw invalid("Invalid result");
        if ("EXAM".equals(targetType) && result != null && !java.util.Set.of("PASSED","FAILED").contains(result)) throw invalid("Exam history only supports PASSED or FAILED");
        if (pageable.isUnpaged() || pageable.getPageSize() > 100) throw invalid("size must be between 1 and 100");
        var orders = new ArrayList<org.springframework.data.domain.Sort.Order>();
        pageable.getSort().forEach(order -> {
            if (!SORTS.contains(order.getProperty()) || order.isIgnoreCase()) throw invalid("Unsupported sort field");
            orders.add(order);
        });
        if (orders.isEmpty()) orders.add(org.springframework.data.domain.Sort.Order.desc("completedAt"));
        if (orders.stream().noneMatch(order -> "id".equals(order.getProperty())))
            orders.add(org.springframework.data.domain.Sort.Order.desc("id"));
        var paging=org.springframework.data.domain.PageRequest.of(pageable.getPageNumber(),pageable.getPageSize(),org.springframework.data.domain.Sort.by(orders));
        Specification<LearningResultEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("userId"), userId));
            // Unreleased EXAM rows are excluded even when targetType is omitted.
            predicates.add(cb.or(cb.notEqual(root.get("targetType"),"EXAM"), cb.and(
                    root.get("result").in("PASSED","FAILED"),cb.isNotNull(root.get("completedAt")),
                    cb.isTrue(root.get("resultAvailable")))));
            if (targetType != null) predicates.add(cb.equal(root.get("targetType"), targetType));
            if (targetId != null) predicates.add(cb.equal(root.get("targetId"), targetId));
            if (result != null) predicates.add(cb.equal(root.get("result"), result));
            if (activityType != null) predicates.add(cb.equal(root.get("activityType"),activityType));
            if (licenseClass != null) predicates.add(cb.equal(root.get("licenseClass"),licenseClass));
            if (from != null) predicates.add(cb.greaterThanOrEqualTo(root.get("completedAt"), from));
            if (to != null) predicates.add(cb.lessThanOrEqualTo(root.get("completedAt"), to));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return learningResultRepository.findAll(spec,paging).map(this::item);
    }

    private HistoryItemResponse item(LearningResultEntity e) {
        return HistoryItemResponse.builder().id(e.getId()).sourceType(e.getSourceType()).sourceId(e.getSourceId())
                .targetType(e.getTargetType()).targetId(e.getTargetId()).attemptNo(e.getAttemptNo())
                .activityType(e.getActivityType()).displayTitle(e.getDisplayTitle()).licenseClass(e.getLicenseClass())
                .correctCount(e.getCorrectCount()).questionCount(e.getQuestionCount()).result(e.getResult())
                .score(e.getScore()).maxScore(e.getMaxScore()).progress(e.getProgress()).durationSeconds(e.getDurationSeconds())
                .startedAt(e.getStartedAt()).completedAt(e.getCompletedAt()).resultAvailable(e.getResultAvailable())
                .reviewAvailable(e.getReviewAvailable()).reasonCode(reason(e)).sourceResult(source(e)).build();
    }

    @Transactional(readOnly = true)
    public HistoryDetailResponse getHistoryDetail(UUID userId, UUID resultId) {
        LearningResultEntity e=learningResultRepository.findById(resultId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning result not found"));
        if (!e.getUserId().equals(userId) || "EXAM".equals(e.getTargetType()) &&
                (!Boolean.TRUE.equals(e.getResultAvailable()) || e.getCompletedAt() == null
                        || !java.util.Set.of("PASSED","FAILED").contains(e.getResult())))
            throw new ResourceNotFoundException("Learning result not found");
        boolean exam="EXAM".equals(e.getTargetType());
        return HistoryDetailResponse.builder().id(e.getId()).sourceType(e.getSourceType()).sourceId(e.getSourceId())
                .targetType(e.getTargetType()).targetId(e.getTargetId()).attemptNo(e.getAttemptNo())
                .activityType(e.getActivityType()).displayTitle(e.getDisplayTitle()).licenseClass(e.getLicenseClass())
                .correctCount(e.getCorrectCount()).questionCount(e.getQuestionCount()).result(e.getResult())
                .score(e.getScore()).maxScore(e.getMaxScore()).progress(e.getProgress()).durationSeconds(e.getDurationSeconds())
                .startedAt(e.getStartedAt()).completedAt(e.getCompletedAt()).resultAvailable(e.getResultAvailable())
                .reviewAvailable(e.getReviewAvailable()).reasonCode(reason(e)).sourceResult(source(e))
                .resultSnapshot(exam ? safe(e.getResultSnapshot(),SAFE_SNAPSHOT) : e.getResultSnapshot())
                .metadata(exam ? safe(e.getMetadata(),SAFE_METADATA) : e.getMetadata()).build();
    }
    private java.util.Map<String,Object> safe(java.util.Map<String,Object> input, java.util.Set<String> keys) {
        var output=new java.util.LinkedHashMap<String,Object>();
        if (input != null) input.forEach((key,value) -> {
            if (keys.contains(key) && (value instanceof String || value instanceof Number || value instanceof Boolean)) output.put(key,value);
        });
        return output;
    }
    private com.dts.result.api.response.SourceResultResponse source(LearningResultEntity e) {
        if (!Boolean.TRUE.equals(e.getResultAvailable()) || e.getSourceId() == null) return null;
        String prefix = "PRACTICE_SESSION".equals(e.getSourceType()) ? "/practice/result/"
                : "EXAM_SESSION".equals(e.getSourceType()) ? "/examination/result/" : null;
        return prefix == null ? null : new com.dts.result.api.response.SourceResultResponse(e.getSourceType(),e.getSourceId(),prefix+e.getSourceId());
    }
    private String reason(LearningResultEntity e) {
        return e.getReviewAvailable() == null ? "SOURCE_POLICY_REQUIRED" : Boolean.TRUE.equals(e.getReviewAvailable()) ? null : "REVIEW_NOT_ALLOWED";
    }
    private com.dts.result.api.exception.ValidationException invalid(String message) {
        return new com.dts.result.api.exception.ValidationException(message);
    }

    @Transactional(readOnly = true)
    public List<RecentActivityResponse> getRecentActivities(UUID userId, int limit) {
        log.info("Fetching recent activities for userId: {}, limit: {}", userId, limit);
        
        if (limit < 1 || limit > 100) throw invalid("limit must be between 1 and 100");
        List<LearningResultEntity> recentResults = learningResultRepository.findTop10ByUserIdOrderByCompletedAtDesc(userId)
                .stream().filter(e -> !"EXAM".equals(e.getTargetType()) || Boolean.TRUE.equals(e.getResultAvailable())
                    && e.getCompletedAt()!=null && java.util.Set.of("PASSED","FAILED").contains(e.getResult())).toList();
        
        if (recentResults.size() > limit) {
            recentResults = recentResults.subList(0, limit);
        }

        return recentResults.stream()
                .map(entity -> RecentActivityResponse.builder()
                        .targetType(entity.getTargetType())
                        .targetId(entity.getTargetId())
                        .sourceId(entity.getSourceId())
                        .result(entity.getResult())
                        .score(entity.getScore())
                        .completedAt(entity.getCompletedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
