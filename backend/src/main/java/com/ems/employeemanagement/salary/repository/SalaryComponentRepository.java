
package com.ems.employeemanagement.salary.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ems.employeemanagement.salary.entity.SalaryComponent;
import com.ems.employeemanagement.salary.entity.SalaryStructure;

@Repository
public interface SalaryComponentRepository
        extends JpaRepository<SalaryComponent, Long> {

    List<SalaryComponent> findBySalaryStructure(SalaryStructure salaryStructure);

    void deleteBySalaryStructure(SalaryStructure salaryStructure);
}
