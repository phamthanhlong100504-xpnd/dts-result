package com.dts.result.api.controller;

import com.dts.result.api.response.OverviewResponse;
import com.dts.result.api.response.SummaryItemResponse;
import com.dts.result.application.service.InternalResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/results")
@RequiredArgsConstructor
public class InternalResultController {

    private final InternalResultService internalResultService;

    @GetMapping("/users/{userId}/overview")
    public ResponseEntity<OverviewResponse> getUserOverview(@PathVariable UUID userId) {
        OverviewResponse response = internalResultService.getUserOverview(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}/summaries")
    public ResponseEntity<List<SummaryItemResponse>> getUserSummaries(
            @PathVariable UUID userId,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String status) {
        
        List<SummaryItemResponse> response = internalResultService.getUserSummaries(userId, targetType, status);
        return ResponseEntity.ok(response);
    }
}
