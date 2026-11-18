
package com.ems.employeemanagement.leave.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ems.employeemanagement.leave.entity.LeaveApplication;
import com.ems.employeemanagement.leave.entity.LeaveBalance;
import com.ems.employeemanagement.leave.repository.LeaveApplicationRepository;
import com.ems.employeemanagement.leave.repository.LeaveBalanceRepository;

@Service
public class LeaveService {

    private final LeaveApplicationRepository applicationRepository;
    private final LeaveBalanceRepository balanceRepository;

    public LeaveService(
            LeaveApplicationRepository applicationRepository,
            LeaveBalanceRepository balanceRepository) {

        this.applicationRepository = applicationRepository;
        this.balanceRepository = balanceRepository;
    }

    // Apply for leave
    public LeaveApplication applyLeave(LeaveApplication application) {

        if (application.getEmployeeId() == null) {
            throw new IllegalArgumentException("Employee ID is required");
        }

        if (application.getLeaveType() == null ||
                application.getLeaveType().isBlank()) {
            throw new IllegalArgumentException("Leave type is required");
        }

        if (application.getStartDate() == null ||
                application.getEndDate() == null) {
            throw new IllegalArgumentException("Leave dates are required");
        }

        if (application.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Past dates are not allowed");
        }

        if (application.getEndDate().isBefore(application.getStartDate())) {
            throw new IllegalArgumentException("Invalid leave date range");
        }

        if (application.getReason() == null ||
                application.getReason().isBlank()) {
            throw new IllegalArgumentException("Leave reason is required");
        }

        // Calculate leave days excluding Sundays
        // Monday to Saturday are counted
        int totalDays = 0;

        LocalDate currentDate = application.getStartDate();

        while (!currentDate.isAfter(application.getEndDate())) {

            if (currentDate.getDayOfWeek().getValue() != 7) {
                totalDays++;
            }

            currentDate = currentDate.plusDays(1);
        }

        // Prevent applications containing only Sundays
        if (totalDays == 0) {
            throw new IllegalArgumentException(
                    "Leave application must contain at least one working day");
        }

        // Prevent overlapping pending or approved applications
        boolean overlapping = applicationRepository
                .existsByEmployeeIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        application.getEmployeeId(),
                        List.of("PENDING", "APPROVED"),
                        application.getEndDate(),
                        application.getStartDate()
                );

        if (overlapping) {
            throw new IllegalArgumentException(
                    "You already have a pending or approved leave application for these dates");
        }

        application.setTotalDays(totalDays);
        application.setStatus("PENDING");
        application.setAppliedAt(LocalDateTime.now());
        application.setAdminRemarks(null);
        application.setReviewedAt(null);

        return applicationRepository.save(application);
    }

    // Get employee leave history
    public List<LeaveApplication> getEmployeeLeaves(Long employeeId) {

        return applicationRepository.findByEmployeeId(employeeId);
    }

    // Get employee leave balance
    public List<LeaveBalance> getLeaveBalance(Long employeeId) {

        return balanceRepository.findByEmployeeId(employeeId);
    }

    // Get all pending applications
    public List<LeaveApplication> getPendingLeaves() {

        return applicationRepository.findByStatus("PENDING");
    }

    // Approve leave application
    @Transactional
    public LeaveApplication approveLeave(Long leaveId, String remarks) {

        LeaveApplication application = applicationRepository.findById(leaveId)
                .orElseThrow(() ->
                        new RuntimeException("Leave application not found"));

        if (!"PENDING".equals(application.getStatus())) {
            throw new IllegalStateException(
                    "This application has already been reviewed");
        }

        int year = application.getStartDate().getYear();

        LeaveBalance balance = balanceRepository
                .findByEmployeeIdAndLeaveTypeAndLeaveYear(
                        application.getEmployeeId(),
                        application.getLeaveType(),
                        year)
                .orElseThrow(() ->
                        new RuntimeException("Leave balance not found"));

        if (balance.getRemaining() == null ||
                balance.getRemaining() < application.getTotalDays()) {

            throw new IllegalStateException("Insufficient leave balance");
        }

        int currentUsed = balance.getUsed() == null
                ? 0 : balance.getUsed();

        balance.setUsed(currentUsed + application.getTotalDays());

        balance.setRemaining(
                balance.getRemaining() - application.getTotalDays());

        balanceRepository.save(balance);

        application.setStatus("APPROVED");
        application.setAdminRemarks(remarks);
        application.setReviewedAt(LocalDateTime.now());

        return applicationRepository.save(application);
    }

    // Reject leave application
    public LeaveApplication rejectLeave(Long leaveId, String remarks) {

        LeaveApplication application = applicationRepository.findById(leaveId)
                .orElseThrow(() ->
                        new RuntimeException("Leave application not found"));

        if (!"PENDING".equals(application.getStatus())) {
            throw new IllegalStateException(
                    "This application has already been reviewed");
        }

        application.setStatus("REJECTED");
        application.setAdminRemarks(remarks);
        application.setReviewedAt(LocalDateTime.now());

        return applicationRepository.save(application);
    }
}
