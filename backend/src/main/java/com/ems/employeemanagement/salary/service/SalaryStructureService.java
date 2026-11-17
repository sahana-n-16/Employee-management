
package com.ems.employeemanagement.salary.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ems.employeemanagement.salary.entity.SalaryStructure;
import com.ems.employeemanagement.salary.repository.SalaryStructureRepository;

@Service
public class SalaryStructureService {

    private final SalaryStructureRepository salaryStructureRepository;

    public SalaryStructureService(
            SalaryStructureRepository salaryStructureRepository) {

        this.salaryStructureRepository = salaryStructureRepository;
    }

    // Create a salary structure
    @Transactional
    public SalaryStructure createSalaryStructure(
            SalaryStructure salaryStructure) {

        validateEmployeeId(salaryStructure.getEmployeeId());

        validateSalaryDetails(
            salaryStructure.getAnnualCtc(),
            salaryStructure.getEffectiveFrom(),
            salaryStructure.getEffectiveTo(),
            salaryStructure.getStatus()
        );

        salaryStructure.setStatus(
            normalizeStatus(salaryStructure.getStatus())
        );

        return salaryStructureRepository.save(salaryStructure);
    }

    // Get all salary structures for an employee
    @Transactional(readOnly = true)
    public List<SalaryStructure> getSalaryStructures(Long employeeId) {

        validateEmployeeId(employeeId);

        return salaryStructureRepository.findByEmployeeId(employeeId);
    }

    // Get active salary structures for an employee
    @Transactional(readOnly = true)
    public List<SalaryStructure> getActiveSalaryStructures(
            Long employeeId) {

        validateEmployeeId(employeeId);

        return salaryStructureRepository.findByEmployeeIdAndStatus(
            employeeId, "ACTIVE"
        );
    }

    // Update an existing salary structure
    @Transactional
    public SalaryStructure updateSalaryStructure(
            Long id,
            SalaryStructure updatedDetails) {

        SalaryStructure existing = salaryStructureRepository.findById(id)
            .orElseThrow(() ->
                new RuntimeException("Salary structure not found"));

        validateSalaryDetails(
            updatedDetails.getAnnualCtc(),
            updatedDetails.getEffectiveFrom(),
            updatedDetails.getEffectiveTo(),
            updatedDetails.getStatus()
        );

        // Update salary details without changing employee ID
        existing.setAnnualCtc(updatedDetails.getAnnualCtc());
        existing.setEffectiveFrom(updatedDetails.getEffectiveFrom());
        existing.setEffectiveTo(updatedDetails.getEffectiveTo());

        existing.setStatus(
            normalizeStatus(updatedDetails.getStatus())
        );

        return salaryStructureRepository.save(existing);
    }

    // Validate employee ID
    private void validateEmployeeId(Long employeeId) {

        if (employeeId == null || employeeId <= 0) {
            throw new IllegalArgumentException(
                "Invalid employee ID"
            );
        }
    }

    // Validate salary details
    private void validateSalaryDetails(
            BigDecimal annualCtc,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            String status) {

        if (annualCtc == null ||
            annualCtc.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                "Annual CTC must be greater than zero"
            );
        }

        if (effectiveFrom == null) {
            throw new IllegalArgumentException(
                "Effective from date is required"
            );
        }

        if (effectiveTo != null &&
            effectiveTo.isBefore(effectiveFrom)) {

            throw new IllegalArgumentException(
                "Effective to date cannot be before effective from date"
            );
        }

        normalizeStatus(status);
    }

    // Validate and normalize status
    private String normalizeStatus(String status) {

        if (status == null || status.isBlank()) {
            status = "ACTIVE";
        }

        status = status.trim().toUpperCase();

        if (!status.equals("ACTIVE") &&
            !status.equals("INACTIVE")) {

            throw new IllegalArgumentException(
                "Status must be ACTIVE or INACTIVE"
            );
        }

        return status;
    }
}
