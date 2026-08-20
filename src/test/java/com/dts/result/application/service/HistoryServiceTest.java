package com.dts.result.application.service;

import com.dts.result.api.exception.ResourceNotFoundException;
import com.dts.result.api.response.HistoryDetailResponse;
import com.dts.result.api.response.HistoryItemResponse;
import com.dts.result.api.response.RecentActivityResponse;
import com.dts.result.domain.entity.LearningResultEntity;
import com.dts.result.domain.repository.LearningResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistoryServiceTest {

    @Mock private LearningResultRepository learningResultRepository;
    @InjectMocks private HistoryService historyService;

    private UUID userId;
    private UUID resultId;
    private LearningResultEntity resultEntity;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        resultId = UUID.randomUUID();
        
        resultEntity = new LearningResultEntity();
        resultEntity.setId(resultId);
        resultEntity.setUserId(userId);
        resultEntity.setTargetType("COURSE");
        resultEntity.setTargetId(UUID.randomUUID());
        resultEntity.setResult("PASS");
        resultEntity.setScore(java.math.BigDecimal.valueOf(100));
        resultEntity.setCompletedAt(Instant.now());
    }

    @Test
    @DisplayName("getHistoryList - Happy Case")
    void getHistoryList_HappyCase() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<LearningResultEntity> page = new PageImpl<>(List.of(resultEntity));
        when(learningResultRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<HistoryItemResponse> response = historyService.getHistoryList(userId, "COURSE", UUID.randomUUID(), "PASS", Instant.now().minusSeconds(3600), Instant.now(), pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("COURSE", response.getContent().get(0).getTargetType());
    }

    @Test
    @DisplayName("getHistoryDetail - Not Found")
    void getHistoryDetail_NotFound() {
        when(learningResultRepository.findById(resultId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> historyService.getHistoryDetail(userId, resultId));
    }

    @Test
    @DisplayName("getHistoryDetail - Happy Case")
    void getHistoryDetail_HappyCase() {
        when(learningResultRepository.findById(resultId)).thenReturn(Optional.of(resultEntity));
        HistoryDetailResponse response = historyService.getHistoryDetail(userId, resultId);
        assertNotNull(response);
        assertEquals("PASS", response.getResult());
    }

    @Test
    @DisplayName("getRecentActivities - Happy Case")
    void getRecentActivities_HappyCase() {
        List<LearningResultEntity> list = List.of(resultEntity);
        when(learningResultRepository.findTop10ByUserIdOrderByCompletedAtDesc(userId)).thenReturn(list);

        List<RecentActivityResponse> responses = historyService.getRecentActivities(userId, 10);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("COURSE", responses.get(0).getTargetType());
    }
}
