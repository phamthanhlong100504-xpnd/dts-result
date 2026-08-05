package com.dts.result.application.service;

import com.dts.result.api.response.AttemptSummaryResponse;
import com.dts.result.api.response.PageResponse;
import com.dts.result.domain.entity.UserContentAttemptEntity;
import com.dts.result.domain.repository.UserContentAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListAttemptService {

    private final UserContentAttemptRepository attemptRepository;

    @Transactional(readOnly = true)
    public PageResponse<AttemptSummaryResponse> listAttempts(UUID tenantId, UUID userId, int page, int size) {
        log.info("Fetching attempts list for tenant: {}, user: {}, page: {}, size: {}", tenantId, userId, page, size);

        int pageIndex = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<UserContentAttemptEntity> attemptPage = attemptRepository.findByTenantIdAndUserId(tenantId, userId, pageable);

        List<AttemptSummaryResponse> items = attemptPage.getContent().stream()
                .map(attempt -> new AttemptSummaryResponse(
                        attempt.getId(),
                        attempt.getTenantId(),
                        attempt.getUserId(),
                        attempt.getNodeId(),
                        attempt.getContentId(),
                        attempt.getContentType(),
                        attempt.getSeqNo(),
                        attempt.getStartedAt(),
                        attempt.getSubmittedAt(),
                        attempt.getStatus(),
                        attempt.getGradingStatus(),
                        attempt.getScore(),
                        attempt.getMaxScore(),
                        attempt.getFinalScore(),
                        attempt.getIsPassed(),
                        attempt.getTimeTakenSec(),
                        attempt.getCreatedAt()
                ))
                .toList();

        return new PageResponse<>(
                items,
                page,
                size,
                attemptPage.getTotalElements(),
                attemptPage.getTotalPages()
        );
    }
}
