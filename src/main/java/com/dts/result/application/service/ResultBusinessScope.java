package com.dts.result.application.service;

import com.dts.result.api.exception.ValidationException;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.Set;

public final class ResultBusinessScope {
    public static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final Set<String> LICENSES = Set.of("A1","A","B1","B","C1","C",
            "D1","D2","D","BE","C1E","CE","D1E","D2E","DE","A2","B2","E","F");
    private ResultBusinessScope() {}
    public static String license(String value) {
        if (value == null || !LICENSES.contains(value)) throw invalid("Invalid licenseClass");
        return value;
    }
    public static int integer(String value, int minimum, int maximum, String field) {
        try {
            int number=Integer.parseInt(value);
            if(number<minimum || number>maximum) throw new NumberFormatException();
            return number;
        } catch(NumberFormatException ex) { throw invalid("Invalid "+field); }
    }
    public static Window leaderboard(String period, Instant now) {
        if(period==null) throw invalid("Invalid leaderboard period");
        var date=now.atZone(ZONE).toLocalDate();
        LocalDate start;
        LocalDate end;
        switch(period) {
            case "THIS_WEEK" -> {start=date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));end=start.plusWeeks(1);}
            case "THIS_MONTH" -> {start=date.withDayOfMonth(1);end=start.plusMonths(1);}
            case "ALL_TIME" -> {return new Window(period,null,null,now);}
            default -> throw invalid("Invalid leaderboard period");
        }
        return new Window(period,start.atStartOfDay(ZONE).toInstant(),end.atStartOfDay(ZONE).toInstant(),now);
    }
    public static Window analytics(String period, Instant now) {
        if(period==null) throw invalid("Invalid analytics period");
        Instant start=switch(period) {
            case "7D" -> now.minus(Duration.ofDays(7));
            case "30D" -> now.minus(Duration.ofDays(30));
            case "ALL" -> null;
            default -> throw invalid("Invalid analytics period");
        };
        return new Window(period,start,now,now);
    }
    public static ValidationException invalid(String message) {return new ValidationException(message);}
    public record Window(String period, Instant from, Instant to, Instant asOf) {
        public Instant effectiveEnd() { return to==null || to.isAfter(asOf) ? asOf : to; }
    }
}
