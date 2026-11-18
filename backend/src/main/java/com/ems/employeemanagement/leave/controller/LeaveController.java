
package com.ems.employeemanagement.leave.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ems.employeemanagement.leave.entity.LeaveApplication;
import com.ems.employeemanagement.leave.entity.LeaveBalance;
import com.ems.employeemanagement.leave.service.LeaveService;
import com.ems.employeemanagement.leave.service.LeaveBalanceService;

@RestController
@RequestMapping("/api/leaves")
@CrossOrigin(origins = "http://localhost:5173")
public class LeaveController {

    private final LeaveService leaveService;
    private final LeaveBalanceService balanceService;

    public LeaveController(
            LeaveService leaveService,
            LeaveBalanceService balanceService) {
        this.leaveService = leaveService;
        this.balanceService = balanceService;
    }

    // Apply for leave
    @PostMapping("/apply")
    public ResponseEntity<LeaveApplication> applyLeave(
            @RequestBody LeaveApplication application) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(leaveService.applyLeave(application));
    }

    // Get employee leave history
    @GetMapping("/{employeeId}")
    public ResponseEntity<List<LeaveApplication>> getEmployeeLeaves(
            @PathVariable Long employeeId) {

        return ResponseEntity.ok(
                leaveService.getEmployeeLeaves(employeeId));
    }

    // Get employee leave balance
    @GetMapping("/balance/{employeeId}")
    public ResponseEntity<List<LeaveBalance>> getLeaveBalance(
            @PathVariable Long employeeId) {

        return ResponseEntity.ok(
                leaveService.getLeaveBalance(employeeId));
    }

    // Get pending applications (Admin)
    @GetMapping("/admin/pending")
    public ResponseEntity<List<LeaveApplication>> getPendingLeaves() {

        return ResponseEntity.ok(leaveService.getPendingLeaves());
    }

    // Approve leave application (Admin)
    @PutMapping("/admin/{leaveId}/approve")
    public ResponseEntity<LeaveApplication> approveLeave(
            @PathVariable Long leaveId,
            @RequestParam(required = false) String remarks) {

        return ResponseEntity.ok(
                leaveService.approveLeave(leaveId, remarks));
    }

    // Reject leave application (Admin)
    @PutMapping("/admin/{leaveId}/reject")
    public ResponseEntity<LeaveApplication> rejectLeave(
            @PathVariable Long leaveId,
            @RequestParam(required = false) String remarks) {

        return ResponseEntity.ok(
                leaveService.rejectLeave(leaveId, remarks));
    }

    // Create or update employee leave allocation (Admin)
    @PostMapping("/admin/balance")
    public ResponseEntity<LeaveBalance> saveLeaveBalance(
            @RequestBody LeaveBalance balance) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(balanceService.saveLeaveBalance(balance));
    }

    // Get all leave balances (Admin)
    @GetMapping("/admin/balances")
    public ResponseEntity<List<LeaveBalance>> getAllLeaveBalances() {

        return ResponseEntity.ok(
                balanceService.getAllLeaveBalances());
    }
}
