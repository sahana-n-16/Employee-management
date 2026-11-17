
package com.ems.employeemanagement.salary.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(
    name = "monthly_salary",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"employee_id", "salary_month", "salary_year"}
        )
    }
)
public class MonthlySalary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Temporary employee reference.
    // Later connect this to the main Employee entity.
    @NotNull(message = "Employee ID is required")
    @Positive(message = "Employee ID must be positive")
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @NotNull(message = "Salary month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    @Column(name = "salary_month", nullable = false)
    private Integer salaryMonth;

    @NotNull(message = "Salary year is required")
    @Min(value = 2000, message = "Invalid salary year")
    @Column(name = "salary_year", nullable = false)
    private Integer salaryYear;

    @NotNull(message = "Basic salary is required")
    @DecimalMin(value = "0.00", message = "Basic salary cannot be negative")
    @Digits(integer = 10, fraction = 2)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal basicSalary = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", message = "Allowances cannot be negative")
    @Digits(integer = 10, fraction = 2)
    @Column(precision = 12, scale = 2)
    private BigDecimal allowances = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", message = "Bonus cannot be negative")
    @Digits(integer = 10, fraction = 2)
    @Column(precision = 12, scale = 2)
    private BigDecimal bonus = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", message = "Deductions cannot be negative")
    @Digits(integer = 10, fraction = 2)
    @Column(precision = 12, scale = 2)
    private BigDecimal deductions = BigDecimal.ZERO;

    // Calculated by MonthlySalaryService
    @NotNull
    @Digits(integer = 10, fraction = 2)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal grossSalary = BigDecimal.ZERO;

    // Calculated by MonthlySalaryService
    @NotNull
    @Digits(integer = 10, fraction = 2)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal netSalary = BigDecimal.ZERO;

    // PENDING or PAID
    @Column(nullable = false, length = 10)
    private String paymentStatus = "PENDING";

    private LocalDate paymentDate;

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

    public Integer getSalaryMonth() {
        return salaryMonth;
    }

    public void setSalaryMonth(Integer salaryMonth) {
        this.salaryMonth = salaryMonth;
    }

    public Integer getSalaryYear() {
        return salaryYear;
    }

    public void setSalaryYear(Integer salaryYear) {
        this.salaryYear = salaryYear;
    }

    public BigDecimal getBasicSalary() {
        return basicSalary;
    }

    public void setBasicSalary(BigDecimal basicSalary) {
        this.basicSalary = basicSalary;
    }

    public BigDecimal getAllowances() {
        return allowances;
    }

    public void setAllowances(BigDecimal allowances) {
        this.allowances = allowances;
    }

    public BigDecimal getBonus() {
        return bonus;
    }

    public void setBonus(BigDecimal bonus) {
        this.bonus = bonus;
    }

    public BigDecimal getDeductions() {
        return deductions;
    }

    public void setDeductions(BigDecimal deductions) {
        this.deductions = deductions;
    }

    public BigDecimal getGrossSalary() {
        return grossSalary;
    }

    public void setGrossSalary(BigDecimal grossSalary) {
        this.grossSalary = grossSalary;
    }

    public BigDecimal getNetSalary() {
        return netSalary;
    }

    public void setNetSalary(BigDecimal netSalary) {
        this.netSalary = netSalary;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }
}
