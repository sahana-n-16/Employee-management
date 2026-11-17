
package com.ems.employeemanagement.salary.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "salary_structure")
public class SalaryStructure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Temporary employee reference.
    // Connect this to the main Employee entity later.
    // Employee ID is assigned by the controller from the URL.
    @Column(nullable = false)
    private Long employeeId;

    @NotNull(message = "Annual CTC is required")
    @DecimalMin(
        value = "0.00",
        inclusive = false,
        message = "Annual CTC must be greater than zero"
    )
    @Digits(
        integer = 10,
        fraction = 2,
        message = "Annual CTC must have at most 10 integer digits and 2 decimal places"
    )
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal annualCtc;

    @NotNull(message = "Effective from date is required")
    @Column(nullable = false)
    private LocalDate effectiveFrom;

    // Optional for an active salary structure.
    private LocalDate effectiveTo;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public BigDecimal getAnnualCtc() {
        return annualCtc;
    }

    public void setAnnualCtc(BigDecimal annualCtc) {
        this.annualCtc = annualCtc;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDate effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
