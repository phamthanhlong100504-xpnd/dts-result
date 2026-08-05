package com.dts.result.application.service;

import com.dts.result.api.response.ContentResultResponse;
import com.dts.result.application.exception.ResourceNotFoundException;
import com.dts.result.domain.entity.UserContentResultEntity;
import com.dts.result.domain.repository.UserContentResultRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentResultService {

    private final UserContentResultRepository resultRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<ContentResultResponse> getUserProgressTree(UUID tenantId, UUID userId) {
        log.info("Fetching content progress tree for tenant: {}, user: {}", tenantId, userId);
        return resultRepository.findAll().stream()
                .filter(r -> tenantId.equals(r.getTenantId()) && userId.equals(r.getUserId()))
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ContentResultResponse getContentResultDetail(UUID tenantId, UUID id) {
        log.info("Fetching content result detail for tenant: {}, id: {}", tenantId, id);
        UserContentResultEntity entity = resultRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Content result not found with ID: " + id));
        return mapToResponse(entity);
    }

    @Transactional
    public ContentResultResponse recalculateContentResult(UUID tenantId, UUID id) {
        log.info("Recalculating content result for tenant: {}, id: {}", tenantId, id);
        UserContentResultEntity entity = resultRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Content result not found with ID: " + id));

        // Logic recalculate rollup
        if (entity.getAttemptCount() > 0) {
            entity.setPercent(100);
            entity.setStatus("COMPLETED");
        }
        UserContentResultEntity saved = resultRepository.save(entity);
        return mapToResponse(saved);
    }

    private ContentResultResponse mapToResponse(UserContentResultEntity entity) {
        return new ContentResultResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getUserId(),
                entity.getContentId(),
                entity.getContentType(),
                entity.getContentVersionId(),
                entity.getParentNodeId(),
                entity.getContentCode(),
                entity.getStatus(),
                entity.getPercent(),
                entity.getTotalLearnSec(),
                entity.getLearnCount(),
                entity.getAttemptCount(),
                entity.getBestScore(),
                entity.getLastScore(),
                entity.getLastScoreAt(),
                entity.getLastAttemptId(),
                parseJson(entity.getAnswers()),
                parseJson(entity.getMetadata()),
                entity.getStartedAt(),
                entity.getLastActivityAt(),
                entity.getCompletedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private Object parseJson(String jsonString) {
        if (jsonString == null || jsonString.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(jsonString, Object.class);
        } catch (Exception e) {
            return jsonString;
        }
    }
}
