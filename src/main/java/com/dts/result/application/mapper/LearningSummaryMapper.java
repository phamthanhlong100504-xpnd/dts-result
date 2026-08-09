package com.dts.result.application.mapper;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class LearningSummaryMapper {

    public String deriveStatus(BigDecimal progress, String eventResult) {
        if ("PASSED".equals(eventResult) || "COMPLETED".equals(eventResult) || "SUBMITTED".equals(eventResult)) {
            return "COMPLETED";
        }
        
        if (progress == null || progress.compareTo(BigDecimal.ZERO) == 0) {
            return "NOT_STARTED";
        } else if (progress.compareTo(BigDecimal.valueOf(100.0)) >= 0) {
            return "COMPLETED";
        } else {
            return "IN_PROGRESS";
        }
    }

    public Map<String, Object> deriveSnapshot(String eventResult, BigDecimal score, BigDecimal maxScore, Map<String, Object> eventSnapshot) {
        Map<String, Object> summarySnapshot = new HashMap<>();
        
        if (eventSnapshot != null) {
            summarySnapshot.putAll(eventSnapshot);
        }
        
        summarySnapshot.put("passed", "PASSED".equals(eventResult));
        
        return summarySnapshot;
    }
}
