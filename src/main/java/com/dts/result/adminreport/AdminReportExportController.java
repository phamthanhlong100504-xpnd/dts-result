package com.dts.result.adminreport;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Same bounded filters and same reporting grant as the existing aggregate Result Admin endpoints. */
@RestController
@RequestMapping("/v1/results/admin/analytics")
public class AdminReportExportController {
    private final AdminReportService reports;
    public AdminReportExportController(AdminReportService reports){this.reports=reports;}
    @GetMapping(value="/export",produces="text/csv;charset=UTF-8")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') and hasAuthority('PERM_reports:read')")
    public ResponseEntity<byte[]> export(@RequestParam String type,
      @RequestParam(required=false) Instant from,@RequestParam(required=false) Instant to,
      @RequestParam(required=false) String licenseClass,@RequestParam(required=false) String module,
      @RequestParam(required=false) String examMode){
        if(!List.of("results","exp").contains(type))throw new IllegalArgumentException("Unknown export type");
        Instant now=Instant.now();
        AdminReportScope scope=AdminReportScope.of(from,to,licenseClass,module,
            "results".equals(type)?examMode:"ALL",now);
        Map<String,Object> metadata;
        String csv;
        if("results".equals(type)) {
            metadata=reports.overview(scope,now);
            if(!"READY".equals(metadata.get("dataStatus")))
                throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT,"Report projection not ready");
            Map<String,Object> trend=reports.trends(scope,now),breakdown=reports.breakdown(scope,now);
            @SuppressWarnings("unchecked") Map<String,Object> summary=(Map<String,Object>)metadata.get("summary");
            @SuppressWarnings("unchecked") List<Map<String,Object>> daily=(List<Map<String,Object>>)trend.get("dailyTrend");
            @SuppressWarnings("unchecked") List<Map<String,Object>> groups=(List<Map<String,Object>>)breakdown.get("items");
            csv=AdminAnalyticsCsv.overview(metadata,summary,daily,groups);
        }else{
            metadata=reports.exp(scope,now);
            if(!"READY".equals(metadata.get("dataStatus")))
                throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT,"EXP projection not ready");
            @SuppressWarnings("unchecked") List<Map<String,Object>> sources=(List<Map<String,Object>>)metadata.get("bySource");
            csv=AdminAnalyticsCsv.exp(metadata,sources);
        }
        // Export contains aggregates only. No per-learner identifiers or raw responses.
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
          .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"dts-admin-"+type+".csv\"")
          .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
          .body(AdminAnalyticsCsv.encode(csv));
    }
}
