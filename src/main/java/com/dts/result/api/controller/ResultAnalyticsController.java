package com.dts.result.api.controller;

import com.dts.result.api.response.ResultAnalyticsResponse;
import com.dts.result.application.service.ResultAnalyticsService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/results/me/analytics")
@RequiredArgsConstructor
public class ResultAnalyticsController {
    private final ResultAnalyticsService service;
    @GetMapping
    public ResultAnalyticsResponse read(@RequestParam(required=false) String licenseClass,
            @RequestParam(defaultValue="30D") String period) {
        UUID user=(UUID)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return service.read(user,licenseClass,period);
    }
}
