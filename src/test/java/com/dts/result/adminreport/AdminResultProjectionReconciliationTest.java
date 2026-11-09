package com.dts.result.adminreport;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AdminResultProjectionReconciliationTest {
    private final UUID id=new UUID(0,7);
    @Test void onlyExplicitAdminReportGrantCanAccessEndpoint() throws Exception {
        var method=AdminResultProjectionReconciliationController.class.getMethod("inspect",UUID.class,String.class);
        PreAuthorize grant=method.getAnnotation(PreAuthorize.class);
        assertNotNull(grant);assertTrue(grant.value().contains("ROLE_ADMIN"));
        assertTrue(grant.value().contains("PERM_integrations:read"));
    }
    @Test void producerAckIsNeverAssumedFromMissingOrAppliedProjection(){
        JdbcTemplate db=mock(JdbcTemplate.class);
        var source=new AdminResultProjectionReconciliationController.Monitor(db);
        when(db.queryForObject(anyString(),eq(Long.class),eq(id))).thenReturn(0L);
        var missing=source.inspect(id,"THEORY");
        assertEquals("NOT_OBSERVED",missing.get("reconciliationStatus"));
        assertNull(missing.get("producerAcknowledged"));
        assertEquals(false,missing.get("projectionApplied"));
        when(db.queryForMap(anyString(),eq(id))).thenReturn(Map.of("received",1L,"applied",0L));
        var received=source.inspect(id,"SIMULATION");
        assertEquals("RECEIVED_NOT_APPLIED",received.get("reconciliationStatus"));
        assertNull(received.get("sourceCompletionConfirmed"));
    }
    @Test void moduleMustBeExplicitBeforeAnyDatabaseRead(){
        JdbcTemplate db=mock(JdbcTemplate.class);
        var source=new AdminResultProjectionReconciliationController.Monitor(db);
        assertThrows(IllegalArgumentException.class,()->source.inspect(id,"ALL"));
        assertThrows(IllegalArgumentException.class,()->source.inspect(id,null));
        verifyNoInteractions(db);
    }
}
