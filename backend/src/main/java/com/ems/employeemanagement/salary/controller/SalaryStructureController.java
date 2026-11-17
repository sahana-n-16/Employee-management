
package com.ems.employeemanagement.salary.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ems.employeemanagement.salary.entity.SalaryStructure;
import com.ems.employeemanagement.salary.service.SalaryStructureService;

@RestController
@RequestMapping("/api/salary")
@CrossOrigin(origins = "http://localhost:5173")
public class SalaryStructureController {

    private final SalaryStructureService salaryStructureService;

    public SalaryStructureController(
            SalaryStructureService salaryStructureService) {
        this.salaryStructureService = salaryStructureService;
    }

    // Create salary structure
    @PostMapping("/{employeeId}")
    public ResponseEntity<SalaryStructure> createSalaryStructure(
            @PathVariable Long employeeId,
            @Valid @RequestBody SalaryStructure salaryStructure) {

        salaryStructure.setEmployeeId(employeeId);

        SalaryStructure saved =
                salaryStructureService.createSalaryStructure(salaryStructure);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Get all salary structures for an employee
    @GetMapping("/{employeeId}")
    public ResponseEntity<List<SalaryStructure>> getSalaryStructures(
            @PathVariable Long employeeId) {

        return ResponseEntity.ok(
                salaryStructureService.getSalaryStructures(employeeId));
    }

    // Get active salary structures
    @GetMapping("/{employeeId}/active")
    public ResponseEntity<List<SalaryStructure>> getActiveSalaryStructures(
            @PathVariable Long employeeId) {

        return ResponseEntity.ok(
                salaryStructureService.getActiveSalaryStructures(employeeId));
    }

    // Update salary structure
    @PutMapping("/structure/{id}")
    public ResponseEntity<SalaryStructure> updateSalaryStructure(
            @PathVariable Long id,
            @Valid @RequestBody SalaryStructure salaryStructure) {

        return ResponseEntity.ok(
                salaryStructureService.updateSalaryStructure(id, salaryStructure));
    }
}
