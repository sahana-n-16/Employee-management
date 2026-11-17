package com.ems.attendance.dto;

import com.ems.attendance.entity.Attendance;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AttendanceResponse(
        Long id,
        Long employeeId,
        LocalDate attendanceDate,
        LocalDateTime checkInAt,
        LocalDateTime checkOutAt,
        Long workingMinutes,
        String status) {

    public static AttendanceResponse from(Attendance attendance) {
        LocalDateTime checkOutAt = attendance.getCheckOutAt();
        Long workingMinutes = checkOutAt == null
                ? null
                : Duration.between(attendance.getCheckInAt(), checkOutAt).toMinutes();
        return new AttendanceResponse(
                attendance.getId(),
                attendance.getEmployeeId(),
                attendance.getAttendanceDate(),
                attendance.getCheckInAt(),
                checkOutAt,
                workingMinutes,
                checkOutAt == null ? "IN_PROGRESS" : "COMPLETED");
    }
}
