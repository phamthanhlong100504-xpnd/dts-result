package com.dts.result.application.service;

import com.dts.result.api.response.LeaderboardResponse;
import com.dts.result.infrastructure.repository.PerformanceProjectionRepository;
import java.time.Clock;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeaderboardService {
    private final PerformanceProjectionRepository repository;
    private final Clock clock;
    public LeaderboardService(PerformanceProjectionRepository repository,
            @Qualifier("resultBusinessClock") Clock clock) {this.repository=repository;this.clock=clock;}
    @Transactional(readOnly=true)
    public LeaderboardResponse read(UUID user,String license,String period,String page,String size) {
        return repository.leaderboard(user,ResultBusinessScope.license(license),
                ResultBusinessScope.leaderboard(period,clock.instant()),
                ResultBusinessScope.integer(page,0,Integer.MAX_VALUE,"page"),
                ResultBusinessScope.integer(size,1,100,"size"));
    }
}
