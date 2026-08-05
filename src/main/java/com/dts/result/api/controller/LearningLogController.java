package com.dts.result.api.controller;

import com.dts.result.api.form.RecordLearningLogRequest;
import com.dts.result.api.response.LearningLogResponse;
import com.dts.result.api.response.PageResponse;
import com.dts.result.application.service.LearningLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/result-service/learning-logs")
@RequiredArgsConstructor
public class LearningLogController {

    private final LearningLogService learningLogService;

    @PostMapping
    public ResponseEntity<LearningLogResponse> recordLearningLog(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody RecordLearningLogRequest request
    ) {
        LearningLogResponse response = learningLogService.recordLearningLog(tenantId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<LearningLogResponse>> listLearningLogs(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @RequestParam("userId") UUID userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        PageResponse<LearningLogResponse> response = learningLogService.listLearningLogs(tenantId, userId, page, size);
        return ResponseEntity.ok(response);
    }
}
