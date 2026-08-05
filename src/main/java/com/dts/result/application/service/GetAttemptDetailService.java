package com.dts.result.application.service;

import com.dts.result.api.response.AttemptDetailResponse;
import com.dts.result.application.exception.ResourceNotFoundException;
import com.dts.result.domain.entity.UserContentAttemptEntity;
import com.dts.result.domain.repository.UserContentAttemptRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetAttemptDetailService {

    private final UserContentAttemptRepository attemptRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public AttemptDetailResponse getAttemptDetail(UUID tenantId, UUID attemptId) {
        log.info("Fetching attempt detail for tenant: {}, attemptId: {}", tenantId, attemptId);

        UserContentAttemptEntity attempt = attemptRepository.findByTenantIdAndId(tenantId, attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found with ID: " + attemptId));

        Object parsedAnswers = parseJson(attempt.getAnswers());
        Object parsedProctoring = parseJson(attempt.getProctoringData());

        return new AttemptDetailResponse(
                attempt.getId(),
                attempt.getTenantId(),
                attempt.getUserId(),
                attempt.getNodeId(),
                attempt.getContentId(),
                attempt.getContentType(),
                attempt.getSeqNo(),
                attempt.getStartedAt(),
                attempt.getEndedAt(),
                attempt.getSubmittedAt(),
                attempt.getStatus(),
                attempt.getGradingStatus(),
                attempt.getDurationSec(),
                attempt.getTimeTakenSec(),
                attempt.getDeviceKind(),
                attempt.getSessionId(),
                attempt.getContentVersionId(),
                attempt.getSourceService(),
                attempt.getSourceRef(),
                attempt.getScore(),
                attempt.getMaxScore(),
                attempt.getPenaltyScore(),
                attempt.getFinalScore(),
                attempt.getIsPassed(),
                attempt.getIsLate(),
                attempt.getHintUsedCount(),
                parsedAnswers,
                parsedProctoring,
                attempt.getCreatedAt(),
                attempt.getUpdatedAt()
        );
    }

    private Object parseJson(String jsonString) {
        if (jsonString == null || jsonString.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(jsonString, Object.class);
        } catch (Exception e) {
            log.warn("Failed to parse JSON string: {}", jsonString, e);
            return jsonString;
        }
    }
}
