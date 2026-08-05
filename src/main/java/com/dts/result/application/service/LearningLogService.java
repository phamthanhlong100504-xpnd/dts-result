package com.dts.result.application.service;

import com.dts.result.api.form.RecordLearningLogRequest;
import com.dts.result.api.response.LearningLogResponse;
import com.dts.result.api.response.PageResponse;
import com.dts.result.domain.entity.LearningLogEntity;
import com.dts.result.domain.repository.LearningLogRepository;
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

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LearningLogService {

    private final LearningLogRepository learningLogRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public LearningLogResponse recordLearningLog(UUID tenantId, UUID userId, RecordLearningLogRequest request) {
        log.info("Recording learning log for tenant: {}, user: {}, contentId: {}", tenantId, userId, request.contentId());

        LearningLogEntity entity = LearningLogEntity.builder()
                .tenantId(tenantId)
                .userId(userId)
                .nodeId(request.nodeId())
                .contentId(request.contentId())
                .contentType(request.contentType())
                .sessionKind(request.sessionKind())
                .startedAt(request.startedAt())
                .endedAt(request.endedAt())
                .durationSec(request.durationSec())
                .mediaPositionSec(request.mediaPositionSec())
                .documentPageRead(request.documentPageRead())
                .deviceKind(request.deviceKind())
                .metadata(toJson(request.metadata()))
                .build();

        LearningLogEntity saved = learningLogRepository.save(entity);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<LearningLogResponse> listLearningLogs(UUID tenantId, UUID userId, int page, int size) {
        int pageIndex = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<LearningLogEntity> logPage = learningLogRepository.findByTenantIdAndUserId(tenantId, userId, pageable);

        List<LearningLogResponse> items = logPage.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return new PageResponse<>(items, page, size, logPage.getTotalElements(), logPage.getTotalPages());
    }

    private LearningLogResponse mapToResponse(LearningLogEntity entity) {
        return new LearningLogResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getUserId(),
                entity.getNodeId(),
                entity.getContentId(),
                entity.getContentType(),
                entity.getSessionKind(),
                entity.getStartedAt(),
                entity.getEndedAt(),
                entity.getDurationSec(),
                entity.getMediaPositionSec(),
                entity.getDocumentPageRead(),
                entity.getDeviceKind(),
                parseJson(entity.getMetadata()),
                entity.getCreatedAt()
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
