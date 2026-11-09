package com.dts.result.adminreport;

import java.time.Instant;
import java.util.Map;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/results/admin")
public class AdminReportController {
    private final AdminReportService service;
    public AdminReportController(AdminReportService service){this.service=service;}
    private static <T> ResponseEntity<T> safe(T data){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(data);}
    private static AdminReportScope scope(Instant from,Instant to,String licenseClass,String module,String examMode,Instant now){
        return AdminReportScope.of(from,to,licenseClass,module,examMode,now);
    }
    @PreAuthorize("hasAuthority('ROLE_ADMIN') and hasAuthority('PERM_reports:read')")
    @GetMapping("/analytics/overview")
    public ResponseEntity<Map<String,Object>> overview(@RequestParam(required=false) Instant from,@RequestParam(required=false) Instant to,
       @RequestParam(required=false) String licenseClass,@RequestParam(required=false) String module,@RequestParam(required=false) String examMode){
        Instant now=Instant.now();return safe(service.overview(scope(from,to,licenseClass,module,examMode,now),now));
    }
    @PreAuthorize("hasAuthority('ROLE_ADMIN') and hasAuthority('PERM_reports:read')")
    @GetMapping("/analytics/trends")
    public ResponseEntity<Map<String,Object>> trends(@RequestParam(required=false) Instant from,@RequestParam(required=false) Instant to,
       @RequestParam(required=false) String licenseClass,@RequestParam(required=false) String module,@RequestParam(required=false) String examMode){
        Instant now=Instant.now();return safe(service.trends(scope(from,to,licenseClass,module,examMode,now),now));
    }
    @PreAuthorize("hasAuthority('ROLE_ADMIN') and hasAuthority('PERM_reports:read')")
    @GetMapping("/analytics/breakdown")
    public ResponseEntity<Map<String,Object>> breakdown(@RequestParam(required=false) Instant from,@RequestParam(required=false) Instant to,
       @RequestParam(required=false) String licenseClass,@RequestParam(required=false) String module,@RequestParam(required=false) String examMode){
        Instant now=Instant.now();return safe(service.breakdown(scope(from,to,licenseClass,module,examMode,now),now));
    }
    @PreAuthorize("hasAuthority('ROLE_ADMIN') and hasAuthority('PERM_reports:read')")
    @GetMapping("/exp/summary")
    public ResponseEntity<Map<String,Object>> exp(@RequestParam(required=false) Instant from,@RequestParam(required=false) Instant to,
       @RequestParam String licenseClass,@RequestParam(required=false) String module){
        Instant now=Instant.now();return safe(service.exp(scope(from,to,licenseClass,module,"ALL",now),now));
    }
    @PreAuthorize("hasAuthority('ROLE_ADMIN') and hasAuthority('PERM_reports:read')")
    @GetMapping("/leaderboards")
    public ResponseEntity<Map<String,Object>> leaderboard(@RequestParam String licenseClass,
           @RequestParam(required=false) Instant from,@RequestParam(required=false) Instant to,
           @RequestParam(defaultValue="ALL") String module,@RequestParam(defaultValue="20") int limit){
        Instant now=Instant.now();return safe(service.leaderboard(scope(from,to,licenseClass,module,"ALL",now),now,limit));
    }
    @PreAuthorize("hasAuthority('ROLE_ADMIN') and hasAuthority('PERM_integrations:read')")
    @GetMapping("/integrations/events/{eventId}")
    public ResponseEntity<Map<String,Object>> event(@PathVariable java.util.UUID eventId){return safe(service.event(eventId));}
    @PreAuthorize("hasAuthority('ROLE_ADMIN') and hasAuthority('PERM_integrations:read')")
    @GetMapping("/integrations/result-sync")
    public ResponseEntity<Map<String,Object>> integration(){return safe(service.integrationStatus());}
}
