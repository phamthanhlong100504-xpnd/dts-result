package com.dts.result.adminreport;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Projection evidence only. Source Kafka ACK is not the same as consumer application. */
@RestController
@RequestMapping("/v1/results/admin/integrations")
public class AdminResultProjectionReconciliationController {
    private final Monitor report;
    public AdminResultProjectionReconciliationController(Monitor report){this.report=report;}
    @GetMapping("/source-projections/{sourceId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') and hasAuthority('PERM_integrations:read')")
    public ResponseEntity<Map<String,Object>> inspect(@PathVariable UUID sourceId,@RequestParam String module){
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(report.inspect(sourceId,module));
    }

    @Service
    @Transactional(readOnly=true)
    public static class Monitor {
        private final JdbcTemplate jdbc;
        public Monitor(JdbcTemplate jdbc){this.jdbc=jdbc;}
        public Map<String,Object> inspect(UUID sourceId,String module){
            if(sourceId==null || module==null || !Set.of("THEORY","SIMULATION").contains(module))
                throw new IllegalArgumentException("module must be THEORY or SIMULATION");
            long received,applied;
            if("THEORY".equals(module)) {
                Long result=jdbc.queryForObject("SELECT COUNT(DISTINCT id) FROM learning_results WHERE source_id=?",Long.class,sourceId);
                received=applied=result==null?0L:result;
            }else{
                Map<String,Object> counts=jdbc.queryForMap("""
                    SELECT COUNT(*)::bigint received,
                      COUNT(*) FILTER (WHERE applied)::bigint applied
                    FROM simulation_performance_inbox
                    WHERE source_id=? AND event_type IN ('SIMULATION_EXAM_COMPLETED','SIMULATION_PRACTICE_COMPLETED')
                    """,sourceId);
                received=((Number)counts.get("received")).longValue();applied=((Number)counts.get("applied")).longValue();
            }
            var out=new LinkedHashMap<String,Object>();out.put("sourceService","dts-result");
            out.put("asOf",Instant.now());out.put("dataStatus",received>1?"PARTIAL":"READY");
            out.put("module",module);out.put("sourceId",sourceId);
            out.put("consumerEvidenceObserved",received>0);out.put("projectionApplied",applied==1);
            out.put("projectionRows",applied);
            out.put("reconciliationStatus",received==0?"NOT_OBSERVED":applied==0?"RECEIVED_NOT_APPLIED":applied==1 && received==1?"APPLIED":"CONFLICT");
            out.put("producerAcknowledged",null);out.put("sourceCompletionConfirmed",null);
            out.put("warning",received==0?"Absence in Result alone does not prove the producer sent an event":null);
            return out;
        }
    }
}
