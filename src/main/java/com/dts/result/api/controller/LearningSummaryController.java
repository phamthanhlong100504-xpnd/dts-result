package com.dts.result.api.controller;

import com.dts.result.api.response.LearningSummaryResponse;
import com.dts.result.application.service.LearningSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/result-service")
@RequiredArgsConstructor
public class LearningSummaryController {

    private final LearningSummaryService learningSummaryService;

    @GetMapping("/users/{userId}/learning-summaries")
    public ResponseEntity<List<LearningSummaryResponse>> getUserLearningSummaries(
            @RequestHeader("X-User-Id") UUID callerUserId,
            @RequestHeader(value = "X-User-Roles", required = false) String callerRoles,
            @PathVariable("userId") UUID userId
    ) {
        List<LearningSummaryResponse> response = learningSummaryService.getUserLearningSummaries(callerUserId, callerRoles, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/learning-summaries/{id}")
    public ResponseEntity<LearningSummaryResponse> getLearningSummaryDetail(
            @RequestHeader("X-User-Id") UUID callerUserId,
            @RequestHeader(value = "X-User-Roles", required = false) String callerRoles,
            @PathVariable("id") UUID id
    ) {
        LearningSummaryResponse response = learningSummaryService.getLearningSummaryDetail(callerUserId, callerRoles, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/learning-summaries/{id}/recalculate")
    public ResponseEntity<LearningSummaryResponse> recalculateLearningSummary(
            @RequestHeader("X-User-Id") UUID callerUserId,
            @RequestHeader(value = "X-User-Roles", required = false) String callerRoles,
            @PathVariable("id") UUID id
    ) {
        LearningSummaryResponse response = learningSummaryService.recalculateLearningSummary(callerUserId, callerRoles, id);
        return ResponseEntity.ok(response);
    }
}
