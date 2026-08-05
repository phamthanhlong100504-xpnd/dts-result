package com.dts.result.api.form;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record QuestionAnswerForm(
        @NotNull(message = "questionId is required")
        UUID questionId,

        @NotNull(message = "questionVersionId is required")
        UUID questionVersionId,

        @NotNull(message = "answer payload is required")
        Map<String, Object> answer,

        Boolean isCorrect,
        BigDecimal score,
        BigDecimal maxScore
) {
}
