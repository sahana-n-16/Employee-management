
package com.ems.employeemanagement.leave.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ems.employeemanagement.leave.entity.LeaveBalance;

public interface LeaveBalanceRepository
        extends JpaRepository<LeaveBalance, Long> {

    List<LeaveBalance> findByEmployeeId(Long employeeId);

    Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeAndLeaveYear(
        Long employeeId,
        String leaveType,
        Integer leaveYear
    );
}
