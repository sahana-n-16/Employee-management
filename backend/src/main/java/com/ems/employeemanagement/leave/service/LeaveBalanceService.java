
package com.ems.employeemanagement.leave.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ems.employeemanagement.leave.entity.LeaveBalance;
import com.ems.employeemanagement.leave.repository.LeaveBalanceRepository;

@Service
public class LeaveBalanceService {

    private final LeaveBalanceRepository balanceRepository;

    public LeaveBalanceService(LeaveBalanceRepository balanceRepository) {
        this.balanceRepository = balanceRepository;
    }

    // Create or update leave allocation
    public LeaveBalance saveLeaveBalance(LeaveBalance balance) {

        if (balance.getEmployeeId() == null ||
                balance.getLeaveType() == null ||
                balance.getLeaveYear() == null ||
                balance.getTotalAllocated() == null ||
                balance.getTotalAllocated() < 0) {
            throw new IllegalArgumentException("Invalid leave allocation details");
        }

        LeaveBalance existing = balanceRepository
                .findByEmployeeIdAndLeaveTypeAndLeaveYear(
                        balance.getEmployeeId(),
                        balance.getLeaveType(),
                        balance.getLeaveYear())
                .orElse(null);

        if (existing != null) {
            int used = existing.getUsed() == null ? 0 : existing.getUsed();

            if (balance.getTotalAllocated() < used) {
                throw new IllegalArgumentException(
                        "Allocation cannot be less than leave already used");
            }

            existing.setTotalAllocated(balance.getTotalAllocated());
            existing.setRemaining(balance.getTotalAllocated() - used);

            return balanceRepository.save(existing);
        }

        balance.setUsed(0);
        balance.setRemaining(balance.getTotalAllocated());

        return balanceRepository.save(balance);
    }

    // Get all leave balances
    public List<LeaveBalance> getAllLeaveBalances() {
        return balanceRepository.findAll();
    }
}
