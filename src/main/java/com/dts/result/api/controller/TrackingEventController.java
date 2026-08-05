package com.dts.result.api.controller;

import com.dts.result.api.form.RecordTrackingEventRequest;
import com.dts.result.api.response.PageResponse;
import com.dts.result.api.response.TrackingEventResponse;
import com.dts.result.application.service.TrackingEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/result-service/tracking-events")
@RequiredArgsConstructor
public class TrackingEventController {

    private final TrackingEventService trackingEventService;

    @PostMapping
    public ResponseEntity<TrackingEventResponse> recordTrackingEvent(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody RecordTrackingEventRequest request
    ) {
        TrackingEventResponse response = trackingEventService.recordTrackingEvent(tenantId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<TrackingEventResponse>> listTrackingEvents(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @RequestParam("userId") UUID userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        PageResponse<TrackingEventResponse> response = trackingEventService.listTrackingEvents(tenantId, userId, page, size);
        return ResponseEntity.ok(response);
    }
}
