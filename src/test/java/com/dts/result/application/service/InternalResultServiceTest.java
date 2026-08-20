package com.dts.result.application.service;

import com.dts.result.api.response.OverviewResponse;
import com.dts.result.api.response.SummaryItemResponse;
import com.dts.result.domain.entity.LearningSummaryEntity;
import com.dts.result.domain.repository.LearningSummaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternalResultServiceTest {

    @Mock
    private OverviewService overviewService;

    @Mock
    private LearningSummaryRepository summaryRepository;

    @InjectMocks
    private InternalResultService internalResultService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("getUserOverview - Happy Case")
    void getUserOverview() {
        OverviewResponse overview = new OverviewResponse();
        when(overviewService.getOverview(userId)).thenReturn(overview);

        OverviewResponse result = internalResultService.getUserOverview(userId);
        assertNotNull(result);
    }

    @Test
    @DisplayName("getUserSummaries - No Filters")
    void getUserSummaries_NoFilters() {
        LearningSummaryEntity s1 = new LearningSummaryEntity();
        s1.setTargetType("COURSE");
        s1.setStatus("COMPLETED");

        LearningSummaryEntity s2 = new LearningSummaryEntity();
        s2.setTargetType("LESSON");
        s2.setStatus("IN_PROGRESS");

        when(summaryRepository.findByUserId(userId)).thenReturn(List.of(s1, s2));

        List<SummaryItemResponse> results = internalResultService.getUserSummaries(userId, null, null);
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("getUserSummaries - With Filters")
    void getUserSummaries_WithFilters() {
        LearningSummaryEntity s1 = new LearningSummaryEntity();
        s1.setTargetType("COURSE");
        s1.setStatus("COMPLETED");

        LearningSummaryEntity s2 = new LearningSummaryEntity();
        s2.setTargetType("COURSE");
        s2.setStatus("IN_PROGRESS");

        when(summaryRepository.findByUserId(userId)).thenReturn(List.of(s1, s2));

        List<SummaryItemResponse> results = internalResultService.getUserSummaries(userId, "COURSE", "COMPLETED");
        assertEquals(1, results.size());
        assertEquals("COURSE", results.get(0).getTargetType());
        assertEquals("COMPLETED", results.get(0).getStatus());
    }
}
