
package com.ems.employeemanagement.leave.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ems.employeemanagement.leave.entity.LeaveApplication;

public interface LeaveApplicationRepository
        extends JpaRepository<LeaveApplication, Long> {

    List<LeaveApplication> findByEmployeeId(Long employeeId);

    List<LeaveApplication> findByStatus(String status);

    // Check overlapping pending or approved leave applications
    boolean existsByEmployeeIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long employeeId,
            List<String> statuses,
            LocalDate endDate,
            LocalDate startDate
    );
}
