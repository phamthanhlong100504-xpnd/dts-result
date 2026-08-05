package com.dts.result.api.controller;

import com.dts.result.api.form.SaveNoteRequest;
import com.dts.result.api.form.SaveReactionRequest;
import com.dts.result.api.form.ToggleBookmarkRequest;
import com.dts.result.api.response.BookmarkResponse;
import com.dts.result.api.response.LessonNoteResponse;
import com.dts.result.api.response.ReactionResponse;
import com.dts.result.application.service.UserInteractionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/result-service")
@RequiredArgsConstructor
public class UserInteractionController {

    private final UserInteractionService userInteractionService;

    @PostMapping("/bookmarks/toggle")
    public ResponseEntity<BookmarkResponse> toggleBookmark(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody ToggleBookmarkRequest request
    ) {
        BookmarkResponse response = userInteractionService.toggleBookmark(tenantId, userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/notes")
    public ResponseEntity<LessonNoteResponse> saveNote(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody SaveNoteRequest request
    ) {
        LessonNoteResponse response = userInteractionService.saveNote(tenantId, userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reactions")
    public ResponseEntity<ReactionResponse> saveReaction(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody SaveReactionRequest request
    ) {
        ReactionResponse response = userInteractionService.saveReaction(tenantId, userId, request);
        return ResponseEntity.ok(response);
    }
}
