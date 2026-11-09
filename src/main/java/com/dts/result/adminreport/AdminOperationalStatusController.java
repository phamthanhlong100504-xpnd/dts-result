package com.dts.result.adminreport;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** BE06-09: owner-scoped, no-store operational evidence. No cross-service success is inferred. */
@RestController
public class AdminOperationalStatusController {
    private final AdminReportService monitor;
    public AdminOperationalStatusController(AdminReportService monitor) { this.monitor=monitor; }

    @GetMapping("/v1/results/admin/operations/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') and hasAuthority('PERM_system-status:read')")
    public ResponseEntity<Map<String,Object>> status() {

        Map<String,Object> local=monitor.integrationStatus();
        var out=new LinkedHashMap<String,Object>();out.put("sourceService","dts-result");out.put("asOf",Instant.now());
        out.put("dataStatus","PARTIAL");out.put("localProjectionStatus",local.get("dataStatus"));out.put("consumer",local);
        out.put("sourceProducerStatus",null);out.put("missingSources",List.of("dts-practice","dts-examination"));
        out.put("warnings",List.of("Projection state does not prove all producers have delivered their events"));
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(out);
    }
}
