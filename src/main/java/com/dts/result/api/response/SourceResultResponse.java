package com.dts.result.api.response;

import java.util.UUID;

/** Source owns authorization and review policy, including on every subsequent request. */
public record SourceResultResponse(String sourceType, UUID sourceId, String resultPath) {}
