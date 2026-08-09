package com.dts.result.api.controller;

import com.dts.result.api.response.HistoryDetailResponse;
import com.dts.result.api.response.HistoryItemResponse;
import com.dts.result.api.response.RecentActivityResponse;
import com.dts.result.application.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/results/me")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    private UUID getCurrentUserId() {
        return (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @GetMapping("/history")
    public ResponseEntity<Page<HistoryItemResponse>> getHistoryList(
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) UUID targetId,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @PageableDefault(sort = "completedAt", direction = Sort.Direction.DESC, size = 20) Pageable pageable) {
        
        UUID userId = getCurrentUserId();
        Page<HistoryItemResponse> response = historyService.getHistoryList(userId, targetType, targetId, result, from, to, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{resultId}")
    public ResponseEntity<HistoryDetailResponse> getHistoryDetail(@PathVariable UUID resultId) {
        UUID userId = getCurrentUserId();
        HistoryDetailResponse response = historyService.getHistoryDetail(userId, resultId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<RecentActivityResponse>> getRecentActivities(
            @RequestParam(defaultValue = "10") int limit) {
        UUID userId = getCurrentUserId();
        List<RecentActivityResponse> response = historyService.getRecentActivities(userId, limit);
        return ResponseEntity.ok(response);
    }
}
