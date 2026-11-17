
package com.ems.employeemanagement.salary.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ems.employeemanagement.salary.entity.MonthlySalary;
import com.ems.employeemanagement.salary.repository.MonthlySalaryRepository;

@Service
public class MonthlySalaryService {

    private final MonthlySalaryRepository monthlySalaryRepository;

    public MonthlySalaryService(
            MonthlySalaryRepository monthlySalaryRepository) {

        this.monthlySalaryRepository = monthlySalaryRepository;
    }

    // Create a monthly salary record
    @Transactional
    public MonthlySalary createMonthlySalary(
            Long employeeId,
            MonthlySalary salary) {

        if (employeeId == null || employeeId <= 0) {
            throw new IllegalArgumentException("Invalid employee ID");
        }

        // Check duplicate monthly salary record
        boolean alreadyExists =
                monthlySalaryRepository
                .findByEmployeeIdAndSalaryMonthAndSalaryYear(
                    employeeId,
                    salary.getSalaryMonth(),
                    salary.getSalaryYear()
                ).isPresent();

        if (alreadyExists) {
            throw new IllegalArgumentException(
                "Salary record already exists for this month");
        }

        // Set employee ID from URL
        salary.setEmployeeId(employeeId);

        // Calculate salary
        calculateSalary(salary);

        // Default payment status
        String status = salary.getPaymentStatus();

        if (status == null || status.isBlank()) {
            status = "PENDING";
        }

        status = status.trim().toUpperCase();

        validatePaymentStatus(status);

        if ("PENDING".equals(status)) {
            salary.setPaymentDate(null);
        } else if (salary.getPaymentDate() == null) {
            throw new IllegalArgumentException(
                "Payment date is required for paid salary");
        }

        salary.setPaymentStatus(status);

        return monthlySalaryRepository.save(salary);
    }

    // Get all monthly salary records for an employee
    public List<MonthlySalary> getMonthlySalaries(Long employeeId) {

        return monthlySalaryRepository.findByEmployeeId(employeeId);
    }

    // Get salary for a particular month and year
    public MonthlySalary getMonthlySalary(
            Long employeeId,
            Integer month,
            Integer year) {

        return monthlySalaryRepository
                .findByEmployeeIdAndSalaryMonthAndSalaryYear(
                    employeeId, month, year
                )
                .orElseThrow(() ->
                    new RuntimeException(
                        "Monthly salary record not found"));
    }

    // Update an existing monthly salary record
    @Transactional
    public MonthlySalary updateMonthlySalary(
            Long employeeId,
            Long salaryId,
            MonthlySalary updatedDetails) {

        MonthlySalary existing =
                monthlySalaryRepository.findById(salaryId)
                .orElseThrow(() ->
                    new RuntimeException(
                        "Monthly salary record not found"));

        // Verify employee ownership
        if (!Objects.equals(existing.getEmployeeId(), employeeId)) {
            throw new IllegalArgumentException(
                "Salary record does not belong to this employee");
        }

        // Prevent modification of paid salary records
        if ("PAID".equals(existing.getPaymentStatus())) {
            throw new IllegalArgumentException(
                "Paid salary records cannot be modified");
        }

        // Update salary components
        existing.setBasicSalary(updatedDetails.getBasicSalary());
        existing.setAllowances(updatedDetails.getAllowances());
        existing.setBonus(updatedDetails.getBonus());
        existing.setDeductions(updatedDetails.getDeductions());

        // Recalculate gross and net salary
        calculateSalary(existing);

        return monthlySalaryRepository.save(existing);
    }

    // Mark monthly salary as paid
    @Transactional
    public MonthlySalary markSalaryAsPaid(
            Long employeeId,
            Long salaryId,
            LocalDate paymentDate) {

        MonthlySalary existing =
                monthlySalaryRepository.findById(salaryId)
                .orElseThrow(() ->
                    new RuntimeException(
                        "Monthly salary record not found"));

        // Verify employee ownership
        if (!Objects.equals(existing.getEmployeeId(), employeeId)) {
            throw new IllegalArgumentException(
                "Salary record does not belong to this employee");
        }

        if (paymentDate == null) {
            throw new IllegalArgumentException(
                "Payment date is required");
        }

        if ("PAID".equals(existing.getPaymentStatus())) {
            throw new IllegalArgumentException(
                "Salary has already been marked as paid");
        }

        existing.setPaymentStatus("PAID");
        existing.setPaymentDate(paymentDate);

        return monthlySalaryRepository.save(existing);
    }

    // Calculate gross salary and net salary
    private void calculateSalary(MonthlySalary salary) {

        BigDecimal basic = zeroIfNull(salary.getBasicSalary());
        BigDecimal allowances = zeroIfNull(salary.getAllowances());
        BigDecimal bonus = zeroIfNull(salary.getBonus());
        BigDecimal deductions = zeroIfNull(salary.getDeductions());

        // Check for negative amounts
        if (basic.compareTo(BigDecimal.ZERO) < 0 ||
            allowances.compareTo(BigDecimal.ZERO) < 0 ||
            bonus.compareTo(BigDecimal.ZERO) < 0 ||
            deductions.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                "Salary amounts cannot be negative");
        }

        // Gross = Basic + Allowances + Bonus
        BigDecimal grossSalary = basic
                .add(allowances)
                .add(bonus);

        // Net = Gross - Deductions
        BigDecimal netSalary = grossSalary.subtract(deductions);

        if (netSalary.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                "Deductions cannot exceed gross salary");
        }

        salary.setBasicSalary(basic);
        salary.setAllowances(allowances);
        salary.setBonus(bonus);
        salary.setDeductions(deductions);
        salary.setGrossSalary(grossSalary);
        salary.setNetSalary(netSalary);
    }

    // Validate payment status
    private void validatePaymentStatus(String status) {

        if (!"PENDING".equals(status) && !"PAID".equals(status)) {
            throw new IllegalArgumentException(
                "Payment status must be PENDING or PAID");
        }
    }

    // Helper method for null amounts
    private BigDecimal zeroIfNull(BigDecimal amount) {

        return amount == null ? BigDecimal.ZERO : amount;
    }

    // Delete a pending monthly salary record
    @Transactional
    public void deleteMonthlySalary(
            Long employeeId,
            Long salaryId) {

        MonthlySalary salary =
                monthlySalaryRepository.findById(salaryId)
                .orElseThrow(() ->
                    new RuntimeException(
                        "Monthly salary record not found"));

        // Verify employee ownership
        if (!Objects.equals(salary.getEmployeeId(), employeeId)) {
            throw new IllegalArgumentException(
                "Salary record does not belong to this employee");
        }

        // Prevent deletion of paid salary records
        if ("PAID".equals(salary.getPaymentStatus())) {
            throw new IllegalArgumentException(
                "Paid salary records cannot be deleted");
        }

        monthlySalaryRepository.delete(salary);
    }
}
