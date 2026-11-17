
package com.ems.employeemanagement.salary.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ems.employeemanagement.salary.entity.SalaryComponent;
import com.ems.employeemanagement.salary.entity.SalaryStructure;
import com.ems.employeemanagement.salary.repository.SalaryComponentRepository;
import com.ems.employeemanagement.salary.repository.SalaryStructureRepository;

@Service
public class SalaryComponentService {

    private final SalaryComponentRepository componentRepository;
    private final SalaryStructureRepository structureRepository;

    public SalaryComponentService(
            SalaryComponentRepository componentRepository,
            SalaryStructureRepository structureRepository) {
        this.componentRepository = componentRepository;
        this.structureRepository = structureRepository;
    }

    // Validate salary component details
    private void validateComponent(SalaryComponent component) {

        if (component == null) {
            throw new IllegalArgumentException(
                "Salary component details are required");
        }

        if (component.getComponentName() == null ||
            component.getComponentName().isBlank()) {
            throw new IllegalArgumentException(
                "Component name is required");
        }

        if (component.getComponentName().length() > 100) {
            throw new IllegalArgumentException(
                "Component name cannot exceed 100 characters");
        }

        if (component.getComponentType() == null ||
            component.getComponentType().isBlank()) {
            throw new IllegalArgumentException(
                "Component type is required");
        }

        String type = component.getComponentType().trim().toUpperCase();

        if (!type.equals("EARNING") && !type.equals("DEDUCTION")) {
            throw new IllegalArgumentException(
                "Component type must be EARNING or DEDUCTION");
        }

        component.setComponentType(type);

        BigDecimal amount = component.getAnnualAmount();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                "Annual amount must be greater than zero");
        }

        if (amount.scale() > 2) {
            throw new IllegalArgumentException(
                "Annual amount cannot have more than 2 decimal places");
        }

        if (amount.precision() - amount.scale() > 10) {
            throw new IllegalArgumentException(
                "Annual amount cannot have more than 10 integer digits");
        }
    }

    // Add a component to a salary structure
    @Transactional
    public SalaryComponent addComponent(
            Long salaryStructureId,
            SalaryComponent component) {

        SalaryStructure structure =
                structureRepository.findById(salaryStructureId)
                .orElseThrow(() ->
                    new RuntimeException("Salary structure not found"));

        validateComponent(component);

        component.setId(null);
        component.setSalaryStructure(structure);

        return componentRepository.save(component);
    }

    // Get all components for a salary structure
    @Transactional(readOnly = true)
    public List<SalaryComponent> getComponents(Long salaryStructureId) {

        SalaryStructure structure =
                structureRepository.findById(salaryStructureId)
                .orElseThrow(() ->
                    new RuntimeException("Salary structure not found"));

        return componentRepository.findBySalaryStructure(structure);
    }

    // Update one component
    @Transactional
    public SalaryComponent updateComponent(
            Long salaryStructureId,
            Long componentId,
            SalaryComponent updatedDetails) {

        SalaryStructure structure =
                structureRepository.findById(salaryStructureId)
                .orElseThrow(() ->
                    new RuntimeException("Salary structure not found"));

        SalaryComponent existing =
                componentRepository.findById(componentId)
                .orElseThrow(() ->
                    new RuntimeException("Salary component not found"));

        // Make sure this component belongs to this salary structure
        if (existing.getSalaryStructure() == null ||
            !Objects.equals(
                existing.getSalaryStructure().getId(),
                structure.getId())) {

            throw new IllegalArgumentException(
                "Salary component does not belong to this salary structure");
        }

        validateComponent(updatedDetails);

        existing.setComponentName(updatedDetails.getComponentName().trim());
        existing.setComponentType(updatedDetails.getComponentType());
        existing.setAnnualAmount(updatedDetails.getAnnualAmount());

        return componentRepository.save(existing);
    }

    // Delete one specific component
    @Transactional
    public void deleteComponent(
            Long salaryStructureId,
            Long componentId) {

        SalaryStructure structure =
                structureRepository.findById(salaryStructureId)
                .orElseThrow(() ->
                    new RuntimeException("Salary structure not found"));

        SalaryComponent component =
                componentRepository.findById(componentId)
                .orElseThrow(() ->
                    new RuntimeException("Salary component not found"));

        // Make sure this component belongs to this salary structure
        if (component.getSalaryStructure() == null ||
            !Objects.equals(
                component.getSalaryStructure().getId(),
                structure.getId())) {

            throw new IllegalArgumentException(
                "Salary component does not belong to this salary structure");
        }

        componentRepository.delete(component);
    }

    // Delete all components for a salary structure
    @Transactional
    public void deleteComponents(Long salaryStructureId) {

        SalaryStructure structure =
                structureRepository.findById(salaryStructureId)
                .orElseThrow(() ->
                    new RuntimeException("Salary structure not found"));

        componentRepository.deleteBySalaryStructure(structure);
    }
}
