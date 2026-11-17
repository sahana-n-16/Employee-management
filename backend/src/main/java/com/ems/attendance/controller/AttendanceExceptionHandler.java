package com.ems.attendance.controller;

import com.ems.attendance.dto.AttendanceErrorResponse;
import com.ems.attendance.exception.AttendanceConflictException;
import com.ems.attendance.exception.AttendanceNotFoundException;
import com.ems.attendance.exception.AttendanceValidationException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.transaction.TransactionSystemException;

@RestControllerAdvice(basePackageClasses = AttendanceController.class)
public class AttendanceExceptionHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AttendanceExceptionHandler.class);

    @ExceptionHandler(AttendanceConflictException.class)
    public ResponseEntity<AttendanceErrorResponse> handleConflict(
            AttendanceConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new AttendanceErrorResponse(exception.getMessage(), Map.of()));
    }

    @ExceptionHandler(AttendanceNotFoundException.class)
    public ResponseEntity<AttendanceErrorResponse> handleNotFound(
            AttendanceNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new AttendanceErrorResponse(exception.getMessage(), Map.of()));
    }

    @ExceptionHandler(AttendanceValidationException.class)
    public ResponseEntity<AttendanceErrorResponse> handleValidation(
            AttendanceValidationException exception) {
        return ResponseEntity.badRequest()
                .body(new AttendanceErrorResponse(
                        exception.getMessage(), exception.getFieldErrors()));
    }

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MissingServletRequestParameterException.class,
        MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<AttendanceErrorResponse> handleMalformedRequest(Exception exception) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        if (exception instanceof MissingServletRequestParameterException missingParameter) {
            fieldErrors.put(missingParameter.getParameterName(), "This parameter is required.");
        } else if (exception instanceof MethodArgumentTypeMismatchException mismatch) {
            fieldErrors.put(mismatch.getName(), "The supplied value has an invalid format.");
        }
        return ResponseEntity.badRequest()
                .body(new AttendanceErrorResponse(
                        "Request is missing required values or contains an invalid format.",
                        fieldErrors));
    }

    @ExceptionHandler({DataAccessException.class, TransactionSystemException.class})
    public ResponseEntity<AttendanceErrorResponse> handlePersistenceFailure(Exception exception) {
        LOGGER.error(
                "Attendance persistence failed ({}).",
                exception.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new AttendanceErrorResponse(
                        "Attendance could not be processed.", Map.of()));
    }
}
