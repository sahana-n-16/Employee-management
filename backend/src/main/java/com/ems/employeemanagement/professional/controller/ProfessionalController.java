
package com.ems.employeemanagement.professional.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ems.employeemanagement.professional.entity.EmploymentHistory;
import com.ems.employeemanagement.professional.entity.ProfessionalDetails;
import com.ems.employeemanagement.professional.service.ProfessionalService;

@RestController
@RequestMapping("/api/professional")
@CrossOrigin(origins = "http://localhost:5173")
public class ProfessionalController {

    private final ProfessionalService professionalService;

    public ProfessionalController(ProfessionalService professionalService) {
        this.professionalService = professionalService;
    }

    // Get professional details
    @GetMapping("/{employeeId}")
    public ResponseEntity<ProfessionalDetails> getProfessionalDetails(
            @PathVariable Long employeeId) {

        return ResponseEntity.ok(
                professionalService.getProfessionalDetails(employeeId));
    }

    // Create professional details
    @PostMapping("/{employeeId}")
    public ResponseEntity<ProfessionalDetails> saveProfessionalDetails(
            @PathVariable Long employeeId,
            @Valid @RequestBody ProfessionalDetails details) {

        details.setEmployeeId(employeeId);

        ProfessionalDetails saved =
                professionalService.saveProfessionalDetails(details);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Update professional details
    @PutMapping("/{employeeId}")
    public ResponseEntity<ProfessionalDetails> updateProfessionalDetails(
            @PathVariable Long employeeId,
            @Valid @RequestBody ProfessionalDetails details) {

        ProfessionalDetails existing =
                professionalService.getProfessionalDetails(employeeId);

        existing.setOfficePhone(details.getOfficePhone());
        existing.setCity(details.getCity());
        existing.setPinCode(details.getPinCode());
        existing.setOfficeAddress(details.getOfficeAddress());

        existing.setReportingManager(details.getReportingManager());
        existing.setHrName(details.getHrName());

        existing.setDesignation(details.getDesignation());
        existing.setDepartment(details.getDepartment());

        ProfessionalDetails updated =
                professionalService.saveProfessionalDetails(existing);

        return ResponseEntity.ok(updated);
    }

    // Add previous employment
    @PostMapping("/{employeeId}/employment-history")
    public ResponseEntity<EmploymentHistory> addEmploymentHistory(
            @PathVariable Long employeeId,
            @Valid @RequestBody EmploymentHistory history) {

        EmploymentHistory saved =
                professionalService.addEmploymentHistory(employeeId, history);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Get employment history
    @GetMapping("/{employeeId}/employment-history")
    public ResponseEntity<List<EmploymentHistory>> getEmploymentHistory(
            @PathVariable Long employeeId) {

        return ResponseEntity.ok(
                professionalService.getEmploymentHistory(employeeId));
    }

    // Delete previous employment
    @DeleteMapping("/{employeeId}/employment-history/{historyId}")
    public ResponseEntity<Void> deleteEmploymentHistory(
            @PathVariable Long employeeId,
            @PathVariable Long historyId) {

        professionalService.deleteEmploymentHistory(employeeId, historyId);

        return ResponseEntity.noContent().build();
    }
}
