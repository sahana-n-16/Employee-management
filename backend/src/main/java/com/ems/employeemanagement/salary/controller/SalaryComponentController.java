
package com.ems.employeemanagement.salary.controller;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.ems.employeemanagement.salary.entity.SalaryComponent;
import com.ems.employeemanagement.salary.service.SalaryComponentService;

@RestController
@RequestMapping("/api/salary")
@CrossOrigin(origins = "http://localhost:5173")
@Validated
public class SalaryComponentController {

    private final SalaryComponentService salaryComponentService;

    public SalaryComponentController(
            SalaryComponentService salaryComponentService) {
        this.salaryComponentService = salaryComponentService;
    }

    // Add a salary component
    @PostMapping("/{salaryStructureId}/components")
    public ResponseEntity<SalaryComponent> addComponent(
            @PathVariable @Positive Long salaryStructureId,
            @Valid @RequestBody SalaryComponent component) {

        SalaryComponent saved =
                salaryComponentService.addComponent(
                    salaryStructureId, component);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Get all salary components
    @GetMapping("/{salaryStructureId}/components")
    public ResponseEntity<List<SalaryComponent>> getComponents(
            @PathVariable @Positive Long salaryStructureId) {

        return ResponseEntity.ok(
            salaryComponentService.getComponents(salaryStructureId));
    }

    // Update one salary component
    @PutMapping("/{salaryStructureId}/components/{componentId}")
    public ResponseEntity<SalaryComponent> updateComponent(
            @PathVariable @Positive Long salaryStructureId,
            @PathVariable @Positive Long componentId,
            @Valid @RequestBody SalaryComponent component) {

        SalaryComponent updated =
                salaryComponentService.updateComponent(
                    salaryStructureId, componentId, component);

        return ResponseEntity.ok(updated);
    }

    // Delete one specific salary component
    @DeleteMapping("/{salaryStructureId}/components/{componentId}")
    public ResponseEntity<Void> deleteComponent(
            @PathVariable @Positive Long salaryStructureId,
            @PathVariable @Positive Long componentId) {

        salaryComponentService.deleteComponent(
            salaryStructureId, componentId);

        return ResponseEntity.noContent().build();
    }

    // Delete all components under a salary structure
    @DeleteMapping("/{salaryStructureId}/components")
    public ResponseEntity<Void> deleteComponents(
            @PathVariable @Positive Long salaryStructureId) {

        salaryComponentService.deleteComponents(salaryStructureId);

        return ResponseEntity.noContent().build();
    }
}
