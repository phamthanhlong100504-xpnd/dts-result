package com.dts.result.api.controller;

import com.dts.result.api.response.ProgressResponse;
import com.dts.result.api.response.ResumeResponse;
import com.dts.result.api.response.SummaryDetailResponse;
import com.dts.result.api.response.SummaryItemResponse;
import com.dts.result.api.response.SummaryStatusResponse;
import com.dts.result.application.service.SummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/results/me/summaries")
@RequiredArgsConstructor
public class SummaryController {

    private final SummaryService summaryService;

    private UUID getCurrentUserId() {
        return (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @GetMapping
    public ResponseEntity<Page<SummaryItemResponse>> getSummaryList(
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String status,
            @PageableDefault(sort = "lastActivityAt", direction = Sort.Direction.DESC, size = 20) Pageable pageable) {
        
        UUID userId = getCurrentUserId();
        Page<SummaryItemResponse> response = summaryService.getSummaryList(userId, targetType, status, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{targetType}/{targetId}")
    public ResponseEntity<SummaryDetailResponse> getSummaryDetail(
            @PathVariable String targetType,
            @PathVariable UUID targetId) {
        
        UUID userId = getCurrentUserId();
        SummaryDetailResponse response = summaryService.getSummaryDetail(userId, targetType, targetId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<SummaryStatusResponse> getStatusStatistics() {
        UUID userId = getCurrentUserId();
        SummaryStatusResponse response = summaryService.getStatusStatistics(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/progress")
    public ResponseEntity<ProgressResponse> getProgress(
            @RequestParam String targetType) {
        
        UUID userId = getCurrentUserId();
        ProgressResponse response = summaryService.getProgress(userId, targetType);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/resume")
    public ResponseEntity<ResumeResponse> getResumeTarget() {
        UUID userId = getCurrentUserId();
        ResumeResponse response = summaryService.getResumeTarget(userId);
        
        if (response == null) {
            return ResponseEntity.ok().build(); // Or 204 No Content
        }
        return ResponseEntity.ok(response);
    }
}
