package com.ems.personal.controller;

import com.ems.personal.dto.ApiErrorResponse;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice(basePackageClasses = PersonalDetailsController.class)
public class PersonalDetailsExceptionHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(PersonalDetailsExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(
                    fieldError.getField(),
                    fieldError.getDefaultMessage() == null
                            ? "Invalid value."
                            : fieldError.getDefaultMessage());
        }
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse("Validation failed.", fieldErrors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getConstraintViolations().forEach(violation ->
                fieldErrors.putIfAbsent(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()));
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse("Validation failed.", fieldErrors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableRequest() {
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse("Request body is missing or malformed.", Map.of()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(
            ResponseStatusException exception) {
        String message = exception.getReason() == null
                ? "Request could not be completed."
                : exception.getReason();
        return ResponseEntity.status(exception.getStatusCode())
                .body(new ApiErrorResponse(message, Map.of()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handlePersistenceConstraint(
            DataIntegrityViolationException exception) {
        Throwable cause = exception.getMostSpecificCause();
        if (isDuplicateEmployeeIdViolation(exception)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiErrorResponse(
                            "Personal details already exist for employee ID.", Map.of()));
        }

        if (cause != null) {
            LOGGER.error(
                    "Personal details persistence failed ({}).",
                    cause.getClass().getSimpleName());
        } else {
            LOGGER.error("Personal details persistence failed without a specific cause.");
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse(
                        "Personal details could not be saved.", Map.of()));
    }

    private boolean isDuplicateEmployeeIdViolation(DataIntegrityViolationException exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            String message = cause.getMessage() == null
                    ? ""
                    : cause.getMessage().toLowerCase(Locale.ROOT);

            if (cause instanceof org.hibernate.exception.ConstraintViolationException violation) {
                String constraintName = violation.getConstraintName();
                if (constraintName != null
                        && (constraintName.equalsIgnoreCase("uq_personal_details_employee_id")
                                || constraintName.toLowerCase(Locale.ROOT).contains("employee_id")
                                || constraintName.toLowerCase(Locale.ROOT).contains("personal_details"))) {
                    return true;
                }
                if (constraintName == null && (message.contains("employee_id")
                        || message.contains("personal_details")
                        || message.contains("uq_personal_details_employee_id"))) {
                    return true;
                }
            }

            if (cause instanceof java.sql.SQLException sqlException) {
                boolean duplicateSqlState = sqlException.getErrorCode() == 1062
                        || "23505".equals(sqlException.getSQLState())
                        || "23000".equals(sqlException.getSQLState());
                if (duplicateSqlState && (message.contains("duplicate")
                        || message.contains("unique constraint")
                        || message.contains("unique key")
                        || message.contains("employee_id")
                        || message.contains("personal_details"))) {
                    return true;
                }
            }

            if ((message.contains("duplicate")
                    || message.contains("unique constraint")
                    || message.contains("unique key")
                    || message.contains("constraint violation"))
                    && (message.contains("employee_id")
                            || message.contains("personal_details")
                            || message.contains("uq_personal_details_employee_id"))) {
                return true;
            }
        }
        return false;
    }
}
