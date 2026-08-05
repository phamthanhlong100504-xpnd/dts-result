package com.dts.result.application.service;

import com.dts.result.application.dto.QuestionAnswerDto;
import com.dts.result.application.dto.SubmitAttemptCommand;
import com.dts.result.application.dto.SubmitAttemptResult;
import com.dts.result.application.enums.AttemptStatus;
import com.dts.result.application.enums.GradingStatus;
import com.dts.result.application.enums.ResultStatus;
import com.dts.result.application.exception.BusinessException;
import com.dts.result.domain.entity.UserContentAttemptEntity;
import com.dts.result.domain.entity.UserContentResultEntity;
import com.dts.result.domain.repository.UserContentAttemptRepository;
import com.dts.result.domain.repository.UserContentResultRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmitAttemptService {

    private final UserContentAttemptRepository attemptRepository;
    private final UserContentResultRepository resultRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public SubmitAttemptResult submitAttempt(SubmitAttemptCommand command) {
        log.info("Processing attempt submission for tenant: {}, user: {}, content: {}",
                command.tenantId(), command.userId(), command.contentId());

        if (command.submittedAt().isBefore(command.startedAt())) {
            throw new BusinessException("RES-400-002",
                    "Submitted timestamp cannot be earlier than started timestamp.",
                    HttpStatus.BAD_REQUEST);
        }

        // 1. Derive next sequence number
        int maxSeq = attemptRepository.findMaxSeqNoByTenantIdAndUserIdAndContentId(
                command.tenantId(), command.userId(), command.contentId());
        int seqNo = maxSeq + 1;

        // 2. Evaluate answers and scores
        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal totalMaxScore = BigDecimal.ZERO;
        boolean allGraded = true;

        if (command.answers() != null && !command.answers().isEmpty()) {
            for (QuestionAnswerDto dto : command.answers()) {
                BigDecimal questionMax = dto.maxScore() != null ? dto.maxScore() : BigDecimal.TEN;
                totalMaxScore = totalMaxScore.add(questionMax);

                if (Boolean.TRUE.equals(dto.isCorrect())) {
                    BigDecimal questionScore = dto.score() != null ? dto.score() : questionMax;
                    totalScore = totalScore.add(questionScore);
                } else if (dto.score() != null) {
                    totalScore = totalScore.add(dto.score());
                } else if (dto.isCorrect() == null) {
                    allGraded = false;
                }
            }
        } else {
            totalMaxScore = BigDecimal.valueOf(100);
            totalScore = BigDecimal.valueOf(100);
        }

        BigDecimal penaltyScore = BigDecimal.ZERO;
        BigDecimal finalScore = totalScore.subtract(penaltyScore).max(BigDecimal.ZERO);
        GradingStatus gradingStatus = allGraded ? GradingStatus.graded : GradingStatus.pending;
        boolean isPassed = totalMaxScore.compareTo(BigDecimal.ZERO) > 0
                && finalScore.compareTo(totalMaxScore.multiply(BigDecimal.valueOf(0.5))) >= 0;

        // 3. Serialize JSONB fields
        String answersJson = serializeToJson(command.answers());
        String proctoringJson = serializeToJson(command.proctoringData());

        // 4. Save attempt entity
        UserContentAttemptEntity attempt = UserContentAttemptEntity.builder()
                .tenantId(command.tenantId())
                .userId(command.userId())
                .nodeId(command.nodeId())
                .contentId(command.contentId())
                .contentType(command.contentType().name())
                .seqNo(seqNo)
                .startedAt(command.startedAt())
                .submittedAt(command.submittedAt())
                .endedAt(command.submittedAt())
                .status(AttemptStatus.submitted.name())
                .gradingStatus(gradingStatus.name())
                .durationSec(command.durationSec())
                .timeTakenSec(command.timeTakenSec())
                .deviceKind(command.deviceKind())
                .sessionId(command.sessionId())
                .contentVersionId(command.contentVersionId())
                .sourceService(command.sourceService())
                .sourceRef(command.sourceRef())
                .score(totalScore)
                .maxScore(totalMaxScore)
                .penaltyScore(penaltyScore)
                .finalScore(finalScore)
                .isPassed(isPassed)
                .isLate(false)
                .hintUsedCount(0)
                .answers(answersJson)
                .proctoringData(proctoringJson)
                .build();

        UserContentAttemptEntity savedAttempt = attemptRepository.save(attempt);

        // 5. Update progress node rollups if nodeId or contentId exists
        updateProgressNodeRollup(command, savedAttempt, finalScore, isPassed);

        return new SubmitAttemptResult(
                savedAttempt.getId(),
                savedAttempt.getTenantId(),
                savedAttempt.getUserId(),
                savedAttempt.getContentId(),
                command.contentType(),
                savedAttempt.getSeqNo(),
                savedAttempt.getStartedAt(),
                savedAttempt.getSubmittedAt(),
                savedAttempt.getStatus(),
                savedAttempt.getGradingStatus(),
                savedAttempt.getScore(),
                savedAttempt.getMaxScore(),
                savedAttempt.getPenaltyScore(),
                savedAttempt.getFinalScore(),
                savedAttempt.getIsPassed(),
                savedAttempt.getTimeTakenSec(),
                savedAttempt.getCreatedAt()
        );
    }

    private void updateProgressNodeRollup(SubmitAttemptCommand command, UserContentAttemptEntity savedAttempt,
                                         BigDecimal finalScore, boolean isPassed) {
        Optional<UserContentResultEntity> resultOpt = Optional.empty();
        if (command.nodeId() != null) {
            resultOpt = resultRepository.findByTenantIdAndId(command.tenantId(), command.nodeId());
        }
        if (resultOpt.isEmpty()) {
            resultOpt = resultRepository.findByTenantIdAndUserIdAndContentId(
                    command.tenantId(), command.userId(), command.contentId());
        }

        resultOpt.ifPresent(result -> {
            result.setAttemptCount(result.getAttemptCount() + 1);
            result.setLastScore(finalScore);
            result.setLastScoreAt(savedAttempt.getSubmittedAt());
            result.setLastAttemptId(savedAttempt.getId());
            result.setLastActivityAt(savedAttempt.getSubmittedAt());

            if (result.getBestScore() == null || finalScore.compareTo(result.getBestScore()) > 0) {
                result.setBestScore(finalScore);
            }

            if (isPassed) {
                result.setStatus(ResultStatus.COMPLETED.name());
                result.setPercent(100);
                result.setCompletedAt(savedAttempt.getSubmittedAt());
            }

            resultRepository.save(result);
            log.info("Updated progress node rollup for result ID: {}", result.getId());
        });
    }

    private String serializeToJson(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Error serializing object to JSON", e);
            return null;
        }
    }
}
