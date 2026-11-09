package com.dts.result.adminreport;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Set;

/** Shared, bounded and source-aligned Admin reporting scope. No student /me query is reused. */
public record AdminReportScope(Instant from, Instant to, String licenseClass, String module, String examMode) {
    public static final ZoneId REPORT_ZONE=ZoneId.of("Asia/Ho_Chi_Minh");
    private static final Set<String> LICENSES=Set.of("A1","A","B1","B","C1","C","D1","D2","D","BE","C1E","CE","D1E","D2E","DE","A2","B2","E","F");
    public static AdminReportScope of(Instant from,Instant to,String license,String module,String mode,Instant now) {
        Instant end=to==null?now:to;
        Instant begin=from==null?end.minus(Duration.ofDays(30)):from;
        if(end.isAfter(now.plusSeconds(60)) || !begin.isBefore(end) || Duration.between(begin,end).compareTo(Duration.ofDays(366))>0)
            throw new IllegalArgumentException("Reporting interval must be positive, <=366 days and not in the future");
        if(license!=null && !LICENSES.contains(license))throw new IllegalArgumentException("Unsupported licenseClass");
        String m=module==null?"ALL":module;
        if(!Set.of("ALL","THEORY","SIMULATION").contains(m))throw new IllegalArgumentException("Invalid module");
        String e=mode==null?"ALL":mode;
        if(!Set.of("ALL","RANDOM","PRESET","OFFICIAL").contains(e))throw new IllegalArgumentException("Invalid examMode");
        if("SIMULATION".equals(m) && "OFFICIAL".equals(e))throw new IllegalArgumentException("Simulation has no official examination");
        return new AdminReportScope(begin,end,license,m,e);
    }
}
