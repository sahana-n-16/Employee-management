
package com.ems.employeemanagement.finance.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ems.employeemanagement.finance.entity.FinanceDetails;
import com.ems.employeemanagement.finance.service.FinanceDetailsService;

@RestController
@RequestMapping("/api/finance")
@CrossOrigin(origins = "http://localhost:5173")
public class FinanceDetailsController {

    private final FinanceDetailsService financeDetailsService;

    public FinanceDetailsController(
            FinanceDetailsService financeDetailsService) {
        this.financeDetailsService = financeDetailsService;
    }

    // Get finance details by employee ID
    @GetMapping("/{employeeId}")
    public ResponseEntity<FinanceDetails> getFinanceDetails(
            @PathVariable Long employeeId) {

        return ResponseEntity.ok(
            financeDetailsService.getFinanceDetails(employeeId)
        );
    }

    // Create finance details
    @PostMapping("/{employeeId}")
    public ResponseEntity<FinanceDetails> saveFinanceDetails(
            @PathVariable Long employeeId,
            @Valid @RequestBody FinanceDetails details) {

        details.setEmployeeId(employeeId);

        FinanceDetails saved =
            financeDetailsService.saveFinanceDetails(details);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Update finance details
    @PutMapping("/{employeeId}")
    public ResponseEntity<FinanceDetails> updateFinanceDetails(
            @PathVariable Long employeeId,
            @Valid @RequestBody FinanceDetails details) {

        FinanceDetails updated =
            financeDetailsService.updateFinanceDetails(employeeId, details);

        return ResponseEntity.ok(updated);
    }
}
