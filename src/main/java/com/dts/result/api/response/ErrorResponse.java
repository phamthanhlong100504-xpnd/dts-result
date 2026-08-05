package com.dts.result.api.response;

import java.time.OffsetDateTime;
import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        List<String> details,
        String traceId,
        OffsetDateTime timestamp
) {
    public ErrorResponse(String code, String message, List<String> details, String traceId) {
        this(code, message, details, traceId, OffsetDateTime.now());
    }
}
