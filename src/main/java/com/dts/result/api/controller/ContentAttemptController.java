package com.dts.result.api.controller;

import com.dts.result.api.form.SubmitAttemptRequest;
import com.dts.result.api.response.AttemptDetailResponse;
import com.dts.result.api.response.AttemptSummaryResponse;
import com.dts.result.api.response.PageResponse;
import com.dts.result.api.response.SubmitAttemptResponse;
import com.dts.result.application.dto.QuestionAnswerDto;
import com.dts.result.application.dto.SubmitAttemptCommand;
import com.dts.result.application.dto.SubmitAttemptResult;
import com.dts.result.application.service.GetAttemptDetailService;
import com.dts.result.application.service.ListAttemptService;
import com.dts.result.application.service.SubmitAttemptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/result-service/content-attempts")
@RequiredArgsConstructor
public class ContentAttemptController {

    private final SubmitAttemptService submitAttemptService;
    private final GetAttemptDetailService getAttemptDetailService;
    private final ListAttemptService listAttemptService;

    @PostMapping
    public ResponseEntity<SubmitAttemptResponse> submitAttempt(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody SubmitAttemptRequest request
    ) {
        List<QuestionAnswerDto> answerDtos = request.answers() != null ?
                request.answers().stream()
                        .map(form -> new QuestionAnswerDto(
                                form.questionId(),
                                form.questionVersionId(),
                                form.answer(),
                                form.isCorrect(),
                                form.score(),
                                form.maxScore()
                        ))
                        .toList() : null;

        SubmitAttemptCommand command = new SubmitAttemptCommand(
                tenantId,
                userId,
                request.nodeId(),
                request.contentId(),
                request.contentType(),
                request.startedAt(),
                request.submittedAt(),
                request.durationSec(),
                request.timeTakenSec(),
                request.deviceKind(),
                request.sessionId(),
                request.contentVersionId(),
                request.sourceService(),
                request.sourceRef(),
                answerDtos,
                request.proctoringData()
        );

        SubmitAttemptResult result = submitAttemptService.submitAttempt(command);

        SubmitAttemptResponse response = new SubmitAttemptResponse(
                result.id(),
                result.tenantId(),
                result.userId(),
                result.contentId(),
                result.contentType(),
                result.seqNo(),
                result.startedAt(),
                result.submittedAt(),
                result.status(),
                result.gradingStatus(),
                result.score(),
                result.maxScore(),
                result.penaltyScore(),
                result.finalScore(),
                result.isPassed(),
                result.timeTakenSec(),
                result.createdAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttemptDetailResponse> getAttemptDetail(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable("id") UUID id
    ) {
        AttemptDetailResponse response = getAttemptDetailService.getAttemptDetail(tenantId, id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<AttemptSummaryResponse>> listAttempts(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @RequestParam("userId") UUID userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        PageResponse<AttemptSummaryResponse> response = listAttemptService.listAttempts(tenantId, userId, page, size);
        return ResponseEntity.ok(response);
    }
}
