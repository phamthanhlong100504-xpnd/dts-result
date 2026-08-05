package com.dts.result.application.service;

import com.dts.result.api.form.RecordTrackingEventRequest;
import com.dts.result.api.response.PageResponse;
import com.dts.result.api.response.TrackingEventResponse;
import com.dts.result.domain.entity.TrackingEventEntity;
import com.dts.result.domain.repository.TrackingEventRepository;
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
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingEventService {

    private final TrackingEventRepository trackingEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public TrackingEventResponse recordTrackingEvent(UUID tenantId, UUID userId, RecordTrackingEventRequest request) {
        log.info("Recording tracking event: {} for tenant: {}, user: {}", request.eventType(), tenantId, userId);

        // Check idempotency
        Optional<TrackingEventEntity> existing = trackingEventRepository.findByTenantIdAndIdempotencyKey(tenantId, request.idempotencyKey());
        if (existing.isPresent()) {
            return mapToResponse(existing.get());
        }

        TrackingEventEntity entity = TrackingEventEntity.builder()
                .tenantId(tenantId)
                .userId(userId)
                .eventType(request.eventType())
                .entityKind(request.entityKind())
                .entityId(request.entityId())
                .versionId(request.versionId())
                .versionNo(request.versionNo())
                .language(request.language())
                .blockId(request.blockId())
                .nodePath(request.nodePath())
                .occurredAt(request.occurredAt())
                .source(request.source())
                .idempotencyKey(request.idempotencyKey())
                .payload(toJson(request.payload()))
                .context(toJson(request.context()))
                .createdBy(userId)
                .build();

        TrackingEventEntity saved = trackingEventRepository.save(entity);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<TrackingEventResponse> listTrackingEvents(UUID tenantId, UUID userId, int page, int size) {
        int pageIndex = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "occurredAt"));

        Page<TrackingEventEntity> eventPage = trackingEventRepository.findByTenantIdAndUserId(tenantId, userId, pageable);

        List<TrackingEventResponse> items = eventPage.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return new PageResponse<>(items, page, size, eventPage.getTotalElements(), eventPage.getTotalPages());
    }

    private TrackingEventResponse mapToResponse(TrackingEventEntity entity) {
        return new TrackingEventResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getUserId(),
                entity.getEventType(),
                entity.getEntityKind(),
                entity.getEntityId(),
                entity.getVersionId(),
                entity.getVersionNo(),
                entity.getLanguage(),
                entity.getBlockId(),
                entity.getNodePath(),
                entity.getOccurredAt(),
                entity.getReceivedAt(),
                entity.getSource(),
                entity.getIdempotencyKey(),
                parseJson(entity.getPayload()),
                parseJson(entity.getContext()),
                entity.getCreatedAt()
        );
    }

    private String toJson(Object obj) {
        if (obj == null) return "{}";
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "{}";
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
