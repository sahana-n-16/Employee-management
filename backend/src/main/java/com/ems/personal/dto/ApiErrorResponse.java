package com.ems.personal.dto;

import java.util.Map;

public record ApiErrorResponse(String message, Map<String, String> fieldErrors) {

    public ApiErrorResponse {
        fieldErrors = Map.copyOf(fieldErrors);
    }
}
