package com.dts.result.simulationperformance;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
public class SimulationPerformanceController {
    private final SimulationPerformanceService service;
    public SimulationPerformanceController(SimulationPerformanceService service){this.service=service;}
    private UUID user(){var a=SecurityContextHolder.getContext().getAuthentication();if(a==null || !(a.getPrincipal() instanceof UUID id))throw new org.springframework.security.access.AccessDeniedException("Authentication required");return id;}
    private ResponseEntity<JsonNode> ok(JsonNode value){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(value);}
    @GetMapping("/v1/results/me/analytics/combined")
    public ResponseEntity<JsonNode> analytics(@RequestParam String licenseClass,@RequestParam(defaultValue="7D") String period,
            @RequestParam(required=false) Long watermark,@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) Instant asOf) {
        return ok(service.analytics(user(),licenseClass,period,watermark,asOf));
    }
    @GetMapping("/v1/leaderboards/combined")
    public ResponseEntity<JsonNode> leaderboard(@RequestParam String licenseClass,@RequestParam(defaultValue="THIS_MONTH") String period,
            @RequestParam(defaultValue="0") String page,@RequestParam(defaultValue="10") String size,
            @RequestParam(required=false) Long watermark,@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) Instant asOf) {
        return ok(service.leaderboard(user(),licenseClass,period,page,size,watermark,asOf));
    }
    @GetMapping("/v1/results/me/history/simulations")
    public ResponseEntity<JsonNode> history(@RequestParam String licenseClass,@RequestParam(required=false) String activityType,
            @RequestParam(required=false) String result,@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue="0") String page,@RequestParam(defaultValue="20") String size,
            @RequestParam(defaultValue="completedAt,desc") String sort,@RequestParam(required=false) Long watermark,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) Instant asOf) {
        return ok(service.history(user(),licenseClass,activityType,result,from,to,page,size,sort,watermark,asOf));
    }
}
