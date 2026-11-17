package com.ems.attendance.service;

import com.ems.attendance.entity.Attendance;
import com.ems.attendance.exception.AttendanceConflictException;
import com.ems.attendance.exception.AttendanceNotFoundException;
import com.ems.attendance.exception.AttendanceValidationException;
import com.ems.attendance.repository.AttendanceRepository;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttendanceService {

    private static final String UNIQUE_CONSTRAINT = "uq_attendance_employee_date";

    private final AttendanceRepository attendanceRepository;
    private final Clock attendanceClock;

    public AttendanceService(AttendanceRepository attendanceRepository, Clock attendanceClock) {
        this.attendanceRepository = attendanceRepository;
        this.attendanceClock = attendanceClock;
    }

    @Transactional
    public Attendance checkIn(Long employeeId) {
        validateEmployeeId(employeeId);
        LocalDateTime checkInAt = LocalDateTime.now(attendanceClock);
        LocalDate today = checkInAt.toLocalDate();

        if (attendanceRepository.findByEmployeeIdAndAttendanceDate(employeeId, today).isPresent()) {
            throw new AttendanceConflictException(
                    "Attendance has already been checked in for this employee today.");
        }

        Attendance attendance = new Attendance();
        attendance.setEmployeeId(employeeId);
        attendance.setAttendanceDate(today);
        attendance.setCheckInAt(checkInAt);

        try {
            return attendanceRepository.saveAndFlush(attendance);
        } catch (DataIntegrityViolationException exception) {
            if (isDuplicateAttendance(exception)) {
                throw new AttendanceConflictException(
                        "Attendance has already been checked in for this employee today.");
            }
            throw exception;
        }
    }

    @Transactional
    public Attendance checkOut(Long employeeId) {
        validateEmployeeId(employeeId);
        LocalDateTime checkOutAt = LocalDateTime.now(attendanceClock);
        LocalDate today = checkOutAt.toLocalDate();
        Attendance attendance = attendanceRepository
                .findForUpdateByEmployeeIdAndAttendanceDate(employeeId, today)
                .orElseThrow(() -> new AttendanceNotFoundException(
                        "No check-in was found for this employee today."));

        if (attendance.getCheckOutAt() != null) {
            throw new AttendanceConflictException(
                    "Attendance has already been checked out for this employee today.");
        }

        if (checkOutAt.isBefore(attendance.getCheckInAt())) {
            throw new AttendanceConflictException(
                    "Checkout time cannot be earlier than check-in time.");
        }
        attendance.setCheckOutAt(checkOutAt);
        return attendanceRepository.saveAndFlush(attendance);
    }

    @Transactional(readOnly = true)
    public Attendance getDailyAttendance(Long employeeId, LocalDate date) {
        validateEmployeeId(employeeId);
        if (date == null) {
            throw validationError("date", "Date is required.");
        }
        return attendanceRepository.findByEmployeeIdAndAttendanceDate(employeeId, date)
                .orElseThrow(() -> new AttendanceNotFoundException(
                        "Attendance was not found for the requested date."));
    }

    @Transactional(readOnly = true)
    public List<Attendance> getMonthlyAttendance(Long employeeId, int year, int month) {
        validateEmployeeId(employeeId);
        if (year < 1000 || year > 9999) {
            throw validationError("year", "Year must be between 1000 and 9999.");
        }
        if (month < 1 || month > 12) {
            throw validationError("month", "Month must be between 1 and 12.");
        }

        YearMonth requestedMonth = YearMonth.of(year, month);
        return attendanceRepository
                .findAllByEmployeeIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(
                        employeeId, requestedMonth.atDay(1), requestedMonth.atEndOfMonth());
    }

    private void validateEmployeeId(Long employeeId) {
        if (employeeId == null || employeeId <= 0) {
            throw validationError("employeeId", "Employee ID must be positive.");
        }
    }

    private AttendanceValidationException validationError(String field, String message) {
        return new AttendanceValidationException("Validation failed.", Map.of(field, message));
    }

    private boolean isDuplicateAttendance(DataIntegrityViolationException exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            String message = cause.getMessage() == null
                    ? ""
                    : cause.getMessage().toLowerCase(Locale.ROOT);

            if (cause instanceof ConstraintViolationException violation) {
                String constraintName = violation.getConstraintName();
                if (constraintName != null
                        && constraintName.equalsIgnoreCase(UNIQUE_CONSTRAINT)) {
                    return true;
                }
            }

            if (cause instanceof SQLException sqlException) {
                boolean duplicateKey = sqlException.getErrorCode() == 1062
                        || "23505".equals(sqlException.getSQLState());
                if (duplicateKey && isAttendanceConstraint(message)) {
                    return true;
                }
            }

            if ((message.contains("duplicate") || message.contains("unique constraint"))
                    && isAttendanceConstraint(message)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAttendanceConstraint(String message) {
        return message.contains(UNIQUE_CONSTRAINT)
                || (message.contains("attendance_records")
                        && message.contains("employee_id")
                        && message.contains("attendance_date"));
    }
}
