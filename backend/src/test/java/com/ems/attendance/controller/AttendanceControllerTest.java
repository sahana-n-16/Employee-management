package com.ems.attendance.controller;

import com.ems.attendance.entity.Attendance;
import com.ems.attendance.exception.AttendanceConflictException;
import com.ems.attendance.exception.AttendanceNotFoundException;
import com.ems.attendance.exception.AttendanceValidationException;
import com.ems.attendance.service.AttendanceService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AttendanceControllerTest {

    private MockMvc mockMvc;
    private AttendanceService attendanceService;

    @BeforeEach
    void setUp() {
        attendanceService = mock(AttendanceService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new AttendanceController(attendanceService))
                .setControllerAdvice(new AttendanceExceptionHandler())
                .build();
    }

    @Test
    void checkInReturnsCreatedAndInProgressShape() throws Exception {
        when(attendanceService.checkIn(100001L)).thenReturn(attendance(false));

        mockMvc.perform(post("/api/attendance/100001/check-in"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeId").value(100001))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.checkOutAt").value(nullValue()))
                .andExpect(jsonPath("$.workingMinutes").value(nullValue()));
    }

    @Test
    void checkOutReturnsCompletedShape() throws Exception {
        when(attendanceService.checkOut(100001L)).thenReturn(attendance(true));

        mockMvc.perform(post("/api/attendance/100001/check-out"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.workingMinutes").value(510));
    }

    @Test
    void dailyEndpointReturnsRequestedDateRecord() throws Exception {
        when(attendanceService.getDailyAttendance(
                        100001L, LocalDate.of(2026, 11, 15)))
                .thenReturn(attendance(true));

        mockMvc.perform(get("/api/attendance/100001/daily")
                        .param("date", "2026-11-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attendanceDate").value("2026-11-15"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void monthlyEndpointReturnsOrderedResponseList() throws Exception {
        when(attendanceService.getMonthlyAttendance(100001L, 2026, 11))
                .thenReturn(List.of(attendance(false), attendance(true)));

        mockMvc.perform(get("/api/attendance/100001/monthly")
                        .param("year", "2026")
                        .param("month", "11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].attendanceDate").value("2026-11-15"))
                .andExpect(jsonPath("$[1].status").value("COMPLETED"));
    }

    @Test
    void duplicateCheckInReturnsConflictErrorResponse() throws Exception {
        when(attendanceService.checkIn(100001L))
                .thenThrow(new AttendanceConflictException("Already checked in."));

        mockMvc.perform(post("/api/attendance/100001/check-in"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Already checked in."))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void checkoutWithoutCheckInReturnsNotFoundErrorResponse() throws Exception {
        when(attendanceService.checkOut(100001L))
                .thenThrow(new AttendanceNotFoundException("No check-in found."));

        mockMvc.perform(post("/api/attendance/100001/check-out"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No check-in found."));
    }

    @Test
    void duplicateCheckoutReturnsConflictErrorResponse() throws Exception {
        when(attendanceService.checkOut(100001L))
                .thenThrow(new AttendanceConflictException("Already checked out."));

        mockMvc.perform(post("/api/attendance/100001/check-out"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Already checked out."));
    }

    @Test
    void missingDailyRecordReturnsNotFoundErrorResponse() throws Exception {
        when(attendanceService.getDailyAttendance(
                        100001L, LocalDate.of(2026, 11, 15)))
                .thenThrow(new AttendanceNotFoundException("Attendance not found."));

        mockMvc.perform(get("/api/attendance/100001/daily")
                        .param("date", "2026-11-15"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Attendance not found."));
    }

    @Test
    void invalidMonthReturnsBadRequestFieldError() throws Exception {
        when(attendanceService.getMonthlyAttendance(100001L, 2026, 13))
                .thenThrow(new AttendanceValidationException(
                        "Validation failed.", Map.of("month", "Month must be between 1 and 12.")));

        mockMvc.perform(get("/api/attendance/100001/monthly")
                        .param("year", "2026")
                        .param("month", "13"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.month").value(
                        "Month must be between 1 and 12."));
    }

    @Test
    void malformedDateReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/attendance/100001/daily")
                        .param("date", "not-a-date"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void unexpectedPersistenceFailureReturnsSanitizedAttendanceError() throws Exception {
        when(attendanceService.checkIn(100001L))
                .thenThrow(new DataIntegrityViolationException(
                        "sensitive SQL and database connection details"));

        mockMvc.perform(post("/api/attendance/100001/check-in"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Attendance could not be processed."))
                .andExpect(jsonPath("$.fieldErrors").isEmpty())
                .andExpect(content().string(not(containsString("sensitive SQL"))))
                .andExpect(content().string(not(containsString("database connection"))));
    }

    private Attendance attendance(boolean completed) {
        Attendance attendance = new Attendance();
        attendance.setId(7L);
        attendance.setEmployeeId(100001L);
        attendance.setAttendanceDate(LocalDate.of(2026, 11, 15));
        attendance.setCheckInAt(LocalDateTime.of(2026, 11, 15, 9, 0));
        if (completed) {
            attendance.setCheckOutAt(LocalDateTime.of(2026, 11, 15, 17, 30));
        }
        return attendance;
    }
}
