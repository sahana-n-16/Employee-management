
package com.ems.employeemanagement.salary.controller;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ems.employeemanagement.salary.entity.MonthlySalary;
import com.ems.employeemanagement.salary.service.MonthlySalaryService;

@RestController
@RequestMapping("/api/salary")
@CrossOrigin(origins = "http://localhost:5173")
public class MonthlySalaryController {

    private final MonthlySalaryService monthlySalaryService;

    public MonthlySalaryController(
            MonthlySalaryService monthlySalaryService) {

        this.monthlySalaryService = monthlySalaryService;
    }

    // Create monthly salary record
    @PostMapping("/{employeeId}/monthly")
    public ResponseEntity<MonthlySalary> createMonthlySalary(
            @PathVariable Long employeeId,
            @Valid @RequestBody MonthlySalary salary) {

        MonthlySalary saved =
                monthlySalaryService.createMonthlySalary(
                    employeeId, salary);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Get all monthly salary records
    @GetMapping("/{employeeId}/monthly")
    public ResponseEntity<List<MonthlySalary>> getMonthlySalaries(
            @PathVariable Long employeeId) {

        return ResponseEntity.ok(
            monthlySalaryService.getMonthlySalaries(employeeId));
    }

    // Get salary for a specific month and year
    @GetMapping("/{employeeId}/monthly/{month}/{year}")
    public ResponseEntity<MonthlySalary> getMonthlySalary(
            @PathVariable Long employeeId,
            @PathVariable Integer month,
            @PathVariable Integer year) {

        return ResponseEntity.ok(
            monthlySalaryService.getMonthlySalary(
                employeeId, month, year));
    }

    // Update monthly salary
    @PutMapping("/{employeeId}/monthly/{salaryId}")
    public ResponseEntity<MonthlySalary> updateMonthlySalary(
            @PathVariable Long employeeId,
            @PathVariable Long salaryId,
            @Valid @RequestBody MonthlySalary salary) {

        MonthlySalary updated =
                monthlySalaryService.updateMonthlySalary(
                    employeeId, salaryId, salary);

        return ResponseEntity.ok(updated);
    }

    // Mark salary as paid
    @PatchMapping("/{employeeId}/monthly/{salaryId}/payment")
    public ResponseEntity<MonthlySalary> markSalaryAsPaid(
            @PathVariable Long employeeId,
            @PathVariable Long salaryId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate paymentDate) {

        MonthlySalary updated =
                monthlySalaryService.markSalaryAsPaid(
                    employeeId, salaryId, paymentDate);

        return ResponseEntity.ok(updated);
    }

    // Delete a pending monthly salary record
    @DeleteMapping("/{employeeId}/monthly/{salaryId}")
    public ResponseEntity<Void> deleteMonthlySalary(
            @PathVariable Long employeeId,
            @PathVariable Long salaryId) {

        monthlySalaryService.deleteMonthlySalary(
            employeeId, salaryId);

        return ResponseEntity.noContent().build();
    }
}
