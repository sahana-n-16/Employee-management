package com.ems.employeemanagement.employee.repository;

import com.ems.employeemanagement.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}