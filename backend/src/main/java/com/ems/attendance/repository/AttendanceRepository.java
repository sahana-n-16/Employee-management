package com.ems.attendance.repository;

import com.ems.attendance.entity.Attendance;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByEmployeeIdAndAttendanceDate(
            Long employeeId, LocalDate attendanceDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select attendance
            from Attendance attendance
            where attendance.employeeId = :employeeId
              and attendance.attendanceDate = :attendanceDate
            """)
    Optional<Attendance> findForUpdateByEmployeeIdAndAttendanceDate(
            @Param("employeeId") Long employeeId,
            @Param("attendanceDate") LocalDate attendanceDate);

    List<Attendance> findAllByEmployeeIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(
            Long employeeId, LocalDate startDate, LocalDate endDate);
}
