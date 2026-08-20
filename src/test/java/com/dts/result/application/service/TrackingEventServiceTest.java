package com.dts.result.application.service;

import com.dts.result.api.form.RecordTrackingEventRequest;
import com.dts.result.api.response.PageResponse;
import com.dts.result.api.response.TrackingEventResponse;
import com.dts.result.domain.entity.TrackingEventEntity;
import com.dts.result.domain.repository.TrackingEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackingEventServiceTest {

    @Mock
    private TrackingEventRepository trackingEventRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private TrackingEventService trackingEventService;

    @Captor
    private ArgumentCaptor<TrackingEventEntity> eventCaptor;

    private UUID tenantId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("recordTrackingEvent - Idempotency hit")
    void recordTrackingEvent_Idempotent() {
        RecordTrackingEventRequest req = new RecordTrackingEventRequest(
                "CLICK", "BUTTON", UUID.randomUUID(), UUID.randomUUID(), 1, "vi",
                UUID.randomUUID(), "/path", OffsetDateTime.now(), "WEB", "idem-key", null, null
        );

        TrackingEventEntity existing = new TrackingEventEntity();
        existing.setId(UUID.randomUUID());
        existing.setTenantId(tenantId);
        existing.setUserId(userId);
        
        when(trackingEventRepository.findByTenantIdAndIdempotencyKey(tenantId, "idem-key"))
                .thenReturn(Optional.of(existing));

        TrackingEventResponse response = trackingEventService.recordTrackingEvent(tenantId, userId, req);
        
        assertNotNull(response);
        assertEquals(tenantId, response.tenantId());
        verify(trackingEventRepository, never()).save(any());
    }

    @Test
    @DisplayName("recordTrackingEvent - Create new")
    void recordTrackingEvent_CreateNew() throws Exception {
        Map<String, Object> payload = Map.of("key", "val");
        RecordTrackingEventRequest req = new RecordTrackingEventRequest(
                "CLICK", "BUTTON", UUID.randomUUID(), UUID.randomUUID(), 1, "vi",
                UUID.randomUUID(), "/path", OffsetDateTime.now(), "WEB", "idem-key", payload, null
        );

        when(trackingEventRepository.findByTenantIdAndIdempotencyKey(tenantId, "idem-key"))
                .thenReturn(Optional.empty());
                
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"key\":\"val\"}");
        
        TrackingEventEntity saved = new TrackingEventEntity();
        saved.setId(UUID.randomUUID());
        saved.setTenantId(tenantId);
        saved.setUserId(userId);
        saved.setPayload("{\"key\":\"val\"}");
        
        when(trackingEventRepository.save(any())).thenReturn(saved);
        when(objectMapper.readValue("{\"key\":\"val\"}", Object.class)).thenReturn(payload);

        TrackingEventResponse response = trackingEventService.recordTrackingEvent(tenantId, userId, req);
        
        assertNotNull(response);
        verify(trackingEventRepository).save(eventCaptor.capture());
        TrackingEventEntity captured = eventCaptor.getValue();
        assertEquals(tenantId, captured.getTenantId());
        assertEquals("idem-key", captured.getIdempotencyKey());
    }
    
    @Test
    @DisplayName("recordTrackingEvent - JSON Write Error")
    void recordTrackingEvent_JsonError() throws Exception {
        Map<String, Object> payload = Map.of("key", "val");
        RecordTrackingEventRequest req = new RecordTrackingEventRequest(
                "CLICK", "BUTTON", UUID.randomUUID(), UUID.randomUUID(), 1, "vi",
                UUID.randomUUID(), "/path", OffsetDateTime.now(), "WEB", "idem-key", payload, null
        );

        when(trackingEventRepository.findByTenantIdAndIdempotencyKey(tenantId, "idem-key"))
                .thenReturn(Optional.empty());
                
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("error") {});
        
        TrackingEventEntity saved = new TrackingEventEntity();
        saved.setId(UUID.randomUUID());
        saved.setPayload("{}");
        
        when(trackingEventRepository.save(any())).thenReturn(saved);
        // It returns "{}" for string parsing.

        TrackingEventResponse response = trackingEventService.recordTrackingEvent(tenantId, userId, req);
        
        assertNotNull(response);
        verify(trackingEventRepository).save(eventCaptor.capture());
        assertEquals("{}", eventCaptor.getValue().getPayload());
    }

    @Test
    @DisplayName("listTrackingEvents - Happy Case")
    void listTrackingEvents_HappyCase() {
        TrackingEventEntity e = new TrackingEventEntity();
        e.setId(UUID.randomUUID());
        e.setTenantId(tenantId);
        e.setUserId(userId);

        Page<TrackingEventEntity> page = new PageImpl<>(List.of(e));
        when(trackingEventRepository.findByTenantIdAndUserId(eq(tenantId), eq(userId), any(Pageable.class))).thenReturn(page);
        
        PageResponse<TrackingEventResponse> res = trackingEventService.listTrackingEvents(tenantId, userId, 1, 10);
        
        assertNotNull(res);
        assertEquals(1, res.items().size());
        assertEquals(1, res.totalItems());
    }
}
