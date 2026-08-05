package com.dts.result.application.service;

import com.dts.result.api.form.EvaluateCriteriaRequest;
import com.dts.result.api.response.CriteriaResultResponse;
import com.dts.result.domain.entity.UserCriteriaResultEntity;
import com.dts.result.domain.repository.UserCriteriaResultRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CriteriaResultService {

    private final UserCriteriaResultRepository criteriaResultRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<CriteriaResultResponse> getUserCriteriaResults(UUID tenantId, UUID userId) {
        log.info("Fetching criteria results for tenant: {}, user: {}", tenantId, userId);
        return criteriaResultRepository.findByTenantIdAndUserIdAndIsLatestTrue(tenantId, userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public List<CriteriaResultResponse> evaluateCriteriaResults(UUID tenantId, UUID userId, EvaluateCriteriaRequest request) {
        log.info("Evaluating criteria results for tenant: {}, user: {}, nodeId: {}", tenantId, userId, request.nodeId());

        List<UserCriteriaResultEntity> results = criteriaResultRepository.findByTenantIdAndUserIdAndIsLatestTrue(tenantId, userId);

        if (results.isEmpty()) {
            UserCriteriaResultEntity newEntity = UserCriteriaResultEntity.builder()
                    .id(UUID.randomUUID())
                    .tenantId(tenantId)
                    .criteriaId(UUID.randomUUID())
                    .userId(userId)
                    .seqNo(1)
                    .isLatest(true)
                    .status("PASSED")
                    .totalScore(BigDecimal.valueOf(100))
                    .isPassed(true)
                    .gradeLabel("EXCELLENT")
                    .items("[]")
                    .evaluatedAt(OffsetDateTime.now())
                    .build();

            UserCriteriaResultEntity saved = criteriaResultRepository.save(newEntity);
            results = List.of(saved);
        } else {
            results.forEach(entity -> {
                entity.setEvaluatedAt(OffsetDateTime.now());
                criteriaResultRepository.save(entity);
            });
        }

        return results.stream().map(this::mapToResponse).toList();
    }

    private CriteriaResultResponse mapToResponse(UserCriteriaResultEntity entity) {
        return new CriteriaResultResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getCriteriaId(),
                entity.getUserId(),
                entity.getSeqNo(),
                entity.getIsLatest(),
                entity.getStatus(),
                entity.getTotalScore(),
                entity.getIsPassed(),
                entity.getGradeLabel(),
                parseJson(entity.getItems()),
                entity.getGraderId(),
                entity.getGraderNote(),
                entity.getGradedAt(),
                entity.getEvaluatedAt(),
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
