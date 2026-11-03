package com.dts.result.api.controller;

import com.dts.result.api.form.RecordLearningResultRequest;
import com.dts.result.api.response.LearningResultResponse;
import com.dts.result.api.response.PageResponse;
import com.dts.result.application.service.LearningResultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/result-service/learning-results")
@RequiredArgsConstructor
public class LearningResultController {

    private final LearningResultService learningResultService;

    @PostMapping
    public ResponseEntity<LearningResultResponse> recordLearningResult(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody RecordLearningResultRequest request
    ) {
        LearningResultResponse response = learningResultService.recordLearningResult(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LearningResultResponse> getLearningResultDetail(
            @RequestHeader("X-User-Id") UUID callerUserId,
            @RequestHeader(value = "X-User-Roles", required = false) String callerRoles,
            @PathVariable("id") UUID id
    ) {
        LearningResultResponse response = learningResultService.getLearningResultDetail(callerUserId, callerRoles, id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<LearningResultResponse>> listLearningResults(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        PageResponse<LearningResultResponse> response = learningResultService.listLearningResults(userId, page, size);
        return ResponseEntity.ok(response);
    }
}
