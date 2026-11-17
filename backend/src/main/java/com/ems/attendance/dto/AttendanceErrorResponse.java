package com.ems.attendance.dto;

import java.util.Map;

public record AttendanceErrorResponse(String message, Map<String, String> fieldErrors) {

    public AttendanceErrorResponse {
        fieldErrors = Map.copyOf(fieldErrors);
    }
}
