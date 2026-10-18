package com.dts.result.api.controller;

import com.dts.result.api.response.LeaderboardResponse;
import com.dts.result.application.service.LeaderboardService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/leaderboards")
@RequiredArgsConstructor
public class LeaderboardController {
    private final LeaderboardService service;
    @GetMapping
    public LeaderboardResponse read(@RequestParam(required=false) String licenseClass,
            @RequestParam(defaultValue="THIS_MONTH") String period,
            @RequestParam(defaultValue="0") String page,@RequestParam(defaultValue="10") String size) {
        UUID user=(UUID)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return service.read(user,licenseClass,period,page,size);
    }
}
