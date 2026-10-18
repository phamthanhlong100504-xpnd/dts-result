package com.dts.result.application.service;

import com.dts.result.api.response.ResultAnalyticsResponse;
import com.dts.result.infrastructure.repository.PerformanceProjectionRepository;
import java.time.Clock;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResultAnalyticsService {
    private final PerformanceProjectionRepository repository;
    private final Clock clock;
    public ResultAnalyticsService(PerformanceProjectionRepository repository,
            @Qualifier("resultBusinessClock") Clock clock) {this.repository=repository;this.clock=clock;}
    @Transactional(readOnly=true)
    public ResultAnalyticsResponse read(UUID user,String license,String period) {
        return repository.analytics(user,ResultBusinessScope.license(license),
                ResultBusinessScope.analytics(period,clock.instant()));
    }
}
