package com.ems.attendance.service;

import com.ems.attendance.entity.Attendance;
import com.ems.attendance.exception.AttendanceConflictException;
import com.ems.attendance.exception.AttendanceNotFoundException;
import com.ems.attendance.exception.AttendanceValidationException;
import com.ems.attendance.repository.AttendanceRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AttendanceServiceTest {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Kolkata");
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 15);

    private AttendanceRepository repository;

    @BeforeEach
    void setUp() {
        repository = mock(AttendanceRepository.class);
    }

    @Test
    void checksInWithServerTime() {
        AttendanceService service = serviceAt("2026-11-15T09:00:00+05:30");
        when(repository.findByEmployeeIdAndAttendanceDate(100001L, TODAY))
                .thenReturn(Optional.empty());
        when(repository.saveAndFlush(any(Attendance.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Attendance result = service.checkIn(100001L);

        assertEquals(100001L, result.getEmployeeId());
        assertEquals(TODAY, result.getAttendanceDate());
        assertEquals(LocalDateTime.of(2026, 11, 15, 9, 0), result.getCheckInAt());
        assertNull(result.getCheckOutAt());
    }

    @Test
    void checksOutAndCalculatesElapsedWholeMinutes() {
        AttendanceService service = serviceAt("2026-11-15T17:30:59+05:30");
        Attendance attendance = attendanceAt("2026-11-15T09:00:00");
        when(repository.findForUpdateByEmployeeIdAndAttendanceDate(100001L, TODAY))
                .thenReturn(Optional.of(attendance));
        when(repository.saveAndFlush(attendance)).thenReturn(attendance);

        Attendance result = service.checkOut(100001L);

        assertEquals(LocalDateTime.of(2026, 11, 15, 17, 30, 59), result.getCheckOutAt());
        assertEquals(510, java.time.Duration.between(
                result.getCheckInAt(), result.getCheckOutAt()).toMinutes());
        verify(repository).saveAndFlush(attendance);
    }

    @Test
    void rejectsDuplicateCheckInAlreadyFoundForToday() {
        AttendanceService service = serviceAt("2026-11-15T09:00:00+05:30");
        Attendance existing = attendanceAt("2026-11-15T08:00:00");
        when(repository.findByEmployeeIdAndAttendanceDate(100001L, TODAY))
                .thenReturn(Optional.of(existing));

        assertThrows(AttendanceConflictException.class, () -> service.checkIn(100001L));
    }

    @Test
    void mapsConcurrentDuplicateCheckInConstraintToConflict() {
        AttendanceService service = serviceAt("2026-11-15T09:00:00+05:30");
        when(repository.findByEmployeeIdAndAttendanceDate(100001L, TODAY))
                .thenReturn(Optional.empty());
        when(repository.saveAndFlush(any(Attendance.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "duplicate key uq_attendance_employee_date"));

        assertThrows(AttendanceConflictException.class, () -> service.checkIn(100001L));
    }

    @Test
    void rejectsCheckoutWithoutCheckIn() {
        AttendanceService service = serviceAt("2026-11-15T17:30:00+05:30");
        when(repository.findForUpdateByEmployeeIdAndAttendanceDate(100001L, TODAY))
                .thenReturn(Optional.empty());

        assertThrows(AttendanceNotFoundException.class, () -> service.checkOut(100001L));
    }

    @Test
    void rejectsDuplicateCheckout() {
        AttendanceService service = serviceAt("2026-11-15T17:30:00+05:30");
        Attendance completed = attendanceAt("2026-11-15T09:00:00");
        completed.setCheckOutAt(LocalDateTime.of(2026, 11, 15, 17, 0));
        when(repository.findForUpdateByEmployeeIdAndAttendanceDate(100001L, TODAY))
                .thenReturn(Optional.of(completed));

        assertThrows(AttendanceConflictException.class, () -> service.checkOut(100001L));
    }

    @Test
    void rejectsNonPositiveEmployeeId() {
        AttendanceService service = serviceAt("2026-11-15T09:00:00+05:30");

        assertThrows(AttendanceValidationException.class, () -> service.checkIn(0L));
    }

    @Test
    void usesRequestedDateForDailyHistory() {
        AttendanceService service = serviceAt("2026-11-15T09:00:00+05:30");
        Attendance expected = attendanceAt("2026-11-15T09:00:00");
        when(repository.findByEmployeeIdAndAttendanceDate(100001L, TODAY))
                .thenReturn(Optional.of(expected));

        assertSame(expected, service.getDailyAttendance(100001L, TODAY));
    }

    @Test
    void acceptsMySqlDateLowerYearBoundary() {
        AttendanceService service = serviceAt("2026-11-15T09:00:00+05:30");
        when(repository
                        .findAllByEmployeeIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(
                                100001L,
                                LocalDate.of(1000, 1, 1),
                                LocalDate.of(1000, 1, 31)))
                .thenReturn(List.of());

        assertTrue(service.getMonthlyAttendance(100001L, 1000, 1).isEmpty());
        verify(repository)
                .findAllByEmployeeIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(
                        100001L,
                        LocalDate.of(1000, 1, 1),
                        LocalDate.of(1000, 1, 31));
    }

    @Test
    void acceptsMySqlDateUpperYearBoundary() {
        AttendanceService service = serviceAt("2026-11-15T09:00:00+05:30");
        when(repository
                        .findAllByEmployeeIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(
                                100001L,
                                LocalDate.of(9999, 12, 1),
                                LocalDate.of(9999, 12, 31)))
                .thenReturn(List.of());

        assertTrue(service.getMonthlyAttendance(100001L, 9999, 12).isEmpty());
        verify(repository)
                .findAllByEmployeeIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(
                        100001L,
                        LocalDate.of(9999, 12, 1),
                        LocalDate.of(9999, 12, 31));
    }

    @Test
    void rejectsYearsOutsideMySqlDateRange() {
        AttendanceService service = serviceAt("2026-11-15T09:00:00+05:30");

        for (int year : List.of(999, 10000)) {
            AttendanceValidationException exception = assertThrows(
                    AttendanceValidationException.class,
                    () -> service.getMonthlyAttendance(100001L, year, 1));
            assertEquals(
                    "Year must be between 1000 and 9999.",
                    exception.getFieldErrors().get("year"));
        }

        verifyNoInteractions(repository);
    }

    private AttendanceService serviceAt(String zonedDateTime) {
        Clock clock = Clock.fixed(OffsetDateTime.parse(zonedDateTime).toInstant(), BUSINESS_ZONE);
        return new AttendanceService(repository, clock);
    }

    private Attendance attendanceAt(String checkInAt) {
        Attendance attendance = new Attendance();
        attendance.setEmployeeId(100001L);
        attendance.setAttendanceDate(TODAY);
        attendance.setCheckInAt(LocalDateTime.parse(checkInAt));
        return attendance;
    }
}
