package com.dts.result.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResultBusinessClockConfig {
    @Bean("resultBusinessClock")
    public Clock resultBusinessClock() { return Clock.systemUTC(); }
}
