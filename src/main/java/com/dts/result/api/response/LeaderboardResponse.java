package com.dts.result.api.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record LeaderboardResponse(String licenseClass, String period, String timezone,
        Instant asOf, Instant from, Instant to, Entry currentUser, List<Entry> items,
        long totalParticipants, int page, int size, long totalPages,
        String ruleVersion, Instant questionCaptureStartedAt) {
    public record Entry(UUID userId, String displayName, String avatarUrl,
            String profileStatus, Long rank, long rankingScore, Instant lastCreditedAt) {}
}
