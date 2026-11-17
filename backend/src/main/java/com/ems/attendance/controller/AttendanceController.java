package com.ems.attendance.controller;

import com.ems.attendance.dto.AttendanceResponse;
import com.ems.attendance.service.AttendanceService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance/{employeeId}")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/check-in")
    public ResponseEntity<AttendanceResponse> checkIn(@PathVariable Long employeeId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AttendanceResponse.from(attendanceService.checkIn(employeeId)));
    }

    @PostMapping("/check-out")
    public ResponseEntity<AttendanceResponse> checkOut(@PathVariable Long employeeId) {
        return ResponseEntity.ok(
                AttendanceResponse.from(attendanceService.checkOut(employeeId)));
    }

    @GetMapping("/daily")
    public ResponseEntity<AttendanceResponse> getDailyAttendance(
            @PathVariable Long employeeId,
            @RequestParam LocalDate date) {
        return ResponseEntity.ok(AttendanceResponse.from(
                attendanceService.getDailyAttendance(employeeId, date)));
    }

    @GetMapping("/monthly")
    public ResponseEntity<List<AttendanceResponse>> getMonthlyAttendance(
            @PathVariable Long employeeId,
            @RequestParam int year,
            @RequestParam int month) {
        List<AttendanceResponse> response = attendanceService
                .getMonthlyAttendance(employeeId, year, month)
                .stream()
                .map(AttendanceResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }
}
