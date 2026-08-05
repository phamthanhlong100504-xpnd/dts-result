package com.dts.result.application.dto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record QuestionAnswerDto(
        UUID questionId,
        UUID questionVersionId,
        Map<String, Object> answer,
        Boolean isCorrect,
        BigDecimal score,
        BigDecimal maxScore
) {
}
