
package com.ems.employeemanagement.salary.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ems.employeemanagement.salary.entity.SalaryStructure;

@Repository
public interface SalaryStructureRepository
        extends JpaRepository<SalaryStructure, Long> {

    // Get all salary structures for an employee
    List<SalaryStructure> findByEmployeeId(Long employeeId);

    // Get active or inactive salary structures for an employee
    List<SalaryStructure> findByEmployeeIdAndStatus(
            Long employeeId,
            String status
    );
}
