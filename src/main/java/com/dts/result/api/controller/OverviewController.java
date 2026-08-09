package com.dts.result.api.controller;

import com.dts.result.api.response.OverviewResponse;
import com.dts.result.application.service.OverviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/results/me/overview")
@RequiredArgsConstructor
public class OverviewController {

    private final OverviewService overviewService;

    private UUID getCurrentUserId() {
        return (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @GetMapping
    public ResponseEntity<OverviewResponse> getOverview() {
        UUID userId = getCurrentUserId();
        OverviewResponse response = overviewService.getOverview(userId);
        return ResponseEntity.ok(response);
    }
}
