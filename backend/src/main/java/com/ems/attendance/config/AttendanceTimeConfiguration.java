package com.ems.attendance.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AttendanceTimeConfiguration {

    public static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Kolkata");

    @Bean
    public Clock attendanceClock() {
        return Clock.system(BUSINESS_ZONE);
    }
}
