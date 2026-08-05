package com.dts.result.api.controller;

import com.dts.result.api.form.EvaluateCriteriaRequest;
import com.dts.result.api.response.CriteriaResultResponse;
import com.dts.result.application.service.CriteriaResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/result-service/users/{userId}/criteria-results")
@RequiredArgsConstructor
public class CriteriaResultController {

    private final CriteriaResultService criteriaResultService;

    @GetMapping
    public ResponseEntity<List<CriteriaResultResponse>> getUserCriteriaResults(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable("userId") UUID userId
    ) {
        List<CriteriaResultResponse> response = criteriaResultService.getUserCriteriaResults(tenantId, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/evaluate")
    public ResponseEntity<List<CriteriaResultResponse>> evaluateCriteriaResults(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable("userId") UUID userId,
            @RequestBody(required = false) EvaluateCriteriaRequest request
    ) {
        EvaluateCriteriaRequest req = request != null ? request : new EvaluateCriteriaRequest(null, "MANUAL");
        List<CriteriaResultResponse> response = criteriaResultService.evaluateCriteriaResults(tenantId, userId, req);
        return ResponseEntity.ok(response);
    }
}
