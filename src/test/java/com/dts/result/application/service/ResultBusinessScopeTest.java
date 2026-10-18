package com.dts.result.application.service;

import com.dts.result.api.exception.ValidationException;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ResultBusinessScopeTest {
    @Test void weekStartsMondayAtVietnamMidnight() {
        Instant boundary=Instant.parse("2026-10-11T17:00:00Z");
        assertEquals(boundary,ResultBusinessScope.leaderboard("THIS_WEEK",boundary).from());
        assertEquals(Instant.parse("2026-10-18T17:00:00Z"),ResultBusinessScope.leaderboard("THIS_WEEK",boundary).to());
        assertEquals(Instant.parse("2026-10-04T17:00:00Z"),ResultBusinessScope.leaderboard("THIS_WEEK",boundary.minusSeconds(1)).from());
    }
    @Test void monthUsesCalendarBoundariesIncludingLeapYear() {
        var window=ResultBusinessScope.leaderboard("THIS_MONTH",Instant.parse("2028-02-20T00:00:00Z"));
        assertEquals(Instant.parse("2028-01-31T17:00:00Z"),window.from());
        assertEquals(Instant.parse("2028-02-29T17:00:00Z"),window.to());
    }
    @Test void analyticsUsesRollingDurationsAndAllTimeHasNoLowerBound() {
        Instant now=Instant.parse("2026-10-16T07:00:00Z");
        assertEquals(Instant.parse("2026-10-09T07:00:00Z"),ResultBusinessScope.analytics("7D",now).from());
        assertEquals(Instant.parse("2026-09-16T07:00:00Z"),ResultBusinessScope.analytics("30D",now).from());
        assertNull(ResultBusinessScope.analytics("ALL",now).from());
        assertNull(ResultBusinessScope.leaderboard("ALL_TIME",now).from());
        assertEquals(now,ResultBusinessScope.leaderboard("ALL_TIME",now).effectiveEnd());
    }
    @Test void invalidScopeAndPaginationAreRejected() {
        for(String value:new String[]{null,"INVALID","a1",""}) assertThrows(ValidationException.class,()->ResultBusinessScope.license(value));
        assertThrows(ValidationException.class,()->ResultBusinessScope.leaderboard("WEEK",Instant.now()));
        assertThrows(ValidationException.class,()->ResultBusinessScope.analytics("THIS_MONTH",Instant.now()));
        assertThrows(ValidationException.class,()->ResultBusinessScope.integer("-1",0,Integer.MAX_VALUE,"page"));
        assertThrows(ValidationException.class,()->ResultBusinessScope.integer("101",1,100,"size"));
        assertThrows(ValidationException.class,()->ResultBusinessScope.integer("abc",1,100,"size"));
    }
}
