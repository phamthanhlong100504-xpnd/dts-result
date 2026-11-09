package com.dts.result.adminreport;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AdminReportScopeTest {
    private final Instant now=Instant.parse("2026-11-07T10:00:00Z");
    @Test void defaultsToBoundedPeriodAndExplicitModule(){
        var s=AdminReportScope.of(null,null,"B",null,null,now);
        assertEquals("ALL",s.module());assertEquals("ALL",s.examMode());assertEquals(now,s.to());
    }
    @Test void rejectsUnboundedOrMisScopedRequests(){
        assertThrows(IllegalArgumentException.class,()->AdminReportScope.of(now,now,"B",null,null,now));
        assertThrows(IllegalArgumentException.class,()->AdminReportScope.of(now.minusSeconds(367L*86400),now,"B",null,null,now));
        assertThrows(IllegalArgumentException.class,()->AdminReportScope.of(null,null,"UNSUPPORTED",null,null,now));
        assertThrows(IllegalArgumentException.class,()->AdminReportScope.of(null,null,"B","SIMULATION","OFFICIAL",now));
    }
    @Test void adminEndpointsHaveSubjectSpecificGrant() throws Exception {
        for(var method:AdminReportController.class.getDeclaredMethods()) {
            if(!java.lang.reflect.Modifier.isPublic(method.getModifiers()) || method.getName().equals("scope"))continue;
            var grant=method.getAnnotation(org.springframework.security.access.prepost.PreAuthorize.class);
            assertNotNull(grant,method.getName());assertTrue(grant.value().contains(method.getName().equals("integration") || method.getName().equals("event")
                ? "PERM_integrations:read" : "PERM_reports:read"));
            assertTrue(grant.value().contains("ROLE_ADMIN"));
        }
    }
}
