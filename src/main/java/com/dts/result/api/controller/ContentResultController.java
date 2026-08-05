package com.dts.result.api.controller;

import com.dts.result.api.response.ContentResultResponse;
import com.dts.result.application.service.ContentResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/result-service")
@RequiredArgsConstructor
public class ContentResultController {

    private final ContentResultService contentResultService;

    @GetMapping("/users/{userId}/content-results")
    public ResponseEntity<List<ContentResultResponse>> getUserProgressTree(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable("userId") UUID userId
    ) {
        List<ContentResultResponse> response = contentResultService.getUserProgressTree(tenantId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/content-results/{id}")
    public ResponseEntity<ContentResultResponse> getContentResultDetail(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable("id") UUID id
    ) {
        ContentResultResponse response = contentResultService.getContentResultDetail(tenantId, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/content-results/{id}/recalculate")
    public ResponseEntity<ContentResultResponse> recalculateContentResult(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable("id") UUID id
    ) {
        ContentResultResponse response = contentResultService.recalculateContentResult(tenantId, id);
        return ResponseEntity.ok(response);
    }
}
