
package com.ems.employeemanagement.salary.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(
    name = "salary_components",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"salary_structure_id", "component_name"}
        )
    }
)
public class SalaryComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Component name is required")
    @Size(max = 100, message = "Component name cannot exceed 100 characters")
    @Column(name = "component_name", nullable = false, length = 100)
    private String componentName;

    @NotBlank(message = "Component type is required")
    @Size(max = 20, message = "Component type cannot exceed 20 characters")
    @Column(name = "component_type", nullable = false, length = 20)
    private String componentType;

    @NotNull(message = "Annual amount is required")
    @DecimalMin(
        value = "0.00",
        inclusive = false,
        message = "Annual amount must be greater than zero"
    )
    @Digits(
        integer = 10,
        fraction = 2,
        message = "Annual amount can have at most 10 integer digits and 2 decimal places"
    )
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal annualAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salary_structure_id", nullable = false)
    private SalaryStructure salaryStructure;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public BigDecimal getAnnualAmount() {
        return annualAmount;
    }

    public void setAnnualAmount(BigDecimal annualAmount) {
        this.annualAmount = annualAmount;
    }

    public SalaryStructure getSalaryStructure() {
        return salaryStructure;
    }

    public void setSalaryStructure(SalaryStructure salaryStructure) {
        this.salaryStructure = salaryStructure;
    }
}
