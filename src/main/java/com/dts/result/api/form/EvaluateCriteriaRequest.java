package com.dts.result.api.form;

import java.util.UUID;

public record EvaluateCriteriaRequest(
        UUID nodeId,
        String triggerSource
) {
}
