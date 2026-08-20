package com.dts.result.application.service;

import com.dts.result.api.form.RecordLearningLogRequest;
import com.dts.result.api.response.LearningLogResponse;
import com.dts.result.api.response.PageResponse;
import com.dts.result.domain.entity.LearningLogEntity;
import com.dts.result.domain.repository.LearningLogRepository;
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

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import java.time.OffsetDateTime;

@ExtendWith(MockitoExtension.class)
class LearningLogServiceTest {

    @Mock
    private LearningLogRepository learningLogRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private LearningLogService learningLogService;

    @Captor
    private ArgumentCaptor<LearningLogEntity> logCaptor;

    private UUID tenantId;
    private UUID userId;
    private UUID contentId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
        contentId = UUID.randomUUID();
    }

    @Test
    @DisplayName("recordLearningLog - Happy Case")
    void recordLearningLog_HappyCase() throws Exception {
        Map<String, Object> meta = Map.of("browser", "Chrome");
        RecordLearningLogRequest req = new RecordLearningLogRequest(
                UUID.randomUUID(), contentId, "LESSON", "VIDEO",
                OffsetDateTime.now().minusSeconds(120), OffsetDateTime.now(), 120, 100, null, "DESKTOP", meta
        );

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"browser\":\"Chrome\"}");
        
        LearningLogEntity savedEntity = new LearningLogEntity();
        savedEntity.setId(UUID.randomUUID());
        savedEntity.setTenantId(tenantId);
        savedEntity.setUserId(userId);
        savedEntity.setContentId(contentId);
        savedEntity.setMetadata("{\"browser\":\"Chrome\"}");
        
        when(learningLogRepository.save(any())).thenReturn(savedEntity);
        when(objectMapper.readValue("{\"browser\":\"Chrome\"}", Object.class)).thenReturn(meta);

        LearningLogResponse response = learningLogService.recordLearningLog(tenantId, userId, req);

        verify(learningLogRepository).save(logCaptor.capture());
        LearningLogEntity captured = logCaptor.getValue();
        
        assertEquals(tenantId, captured.getTenantId());
        assertEquals(userId, captured.getUserId());
        assertEquals(contentId, captured.getContentId());
        assertNotNull(response);
    }

    @Test
    @DisplayName("recordLearningLog - JSON Exception on Write")
    void recordLearningLog_JsonWriteException() throws Exception {
        RecordLearningLogRequest req = new RecordLearningLogRequest(
                UUID.randomUUID(), contentId, "LESSON", "VIDEO",
                OffsetDateTime.now().minusSeconds(120), OffsetDateTime.now(), 120, 100, null, "DESKTOP", java.util.Map.of("a", "b")
        );

        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("error") {});
        
        LearningLogEntity savedEntity = new LearningLogEntity();
        savedEntity.setId(UUID.randomUUID());
        savedEntity.setMetadata(null);
        
        when(learningLogRepository.save(any())).thenReturn(savedEntity);

        LearningLogResponse response = learningLogService.recordLearningLog(tenantId, userId, req);

        verify(learningLogRepository).save(logCaptor.capture());
        assertEquals(null, logCaptor.getValue().getMetadata());
        assertNotNull(response);
    }
    
    @Test
    @DisplayName("listLearningLogs - Happy Case")
    void listLearningLogs_HappyCase() {
        LearningLogEntity entity = new LearningLogEntity();
        entity.setId(UUID.randomUUID());
        entity.setTenantId(tenantId);
        entity.setUserId(userId);
        
        Page<LearningLogEntity> page = new PageImpl<>(List.of(entity));
        when(learningLogRepository.findByTenantIdAndUserId(eq(tenantId), eq(userId), any(Pageable.class))).thenReturn(page);

        PageResponse<LearningLogResponse> response = learningLogService.listLearningLogs(tenantId, userId, 1, 10);
        
        assertNotNull(response);
        assertEquals(1, response.items().size());
        assertEquals(1, response.page());
        assertEquals(10, response.size());
        assertEquals(1, response.totalItems());
    }
}


