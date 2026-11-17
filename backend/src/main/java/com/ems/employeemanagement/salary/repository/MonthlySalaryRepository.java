
package com.ems.employeemanagement.salary.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ems.employeemanagement.salary.entity.MonthlySalary;

@Repository
public interface MonthlySalaryRepository
        extends JpaRepository<MonthlySalary, Long> {

    // Get all monthly salary records for an employee
    List<MonthlySalary> findByEmployeeId(Long employeeId);

    // Get salary record for a particular month and year
    Optional<MonthlySalary> findByEmployeeIdAndSalaryMonthAndSalaryYear(
            Long employeeId,
            Integer salaryMonth,
            Integer salaryYear
    );

}
