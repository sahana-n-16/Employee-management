package com.ems.employeemanagement.project.repository;

import com.ems.employeemanagement.project.entity.EmployeeProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmployeeProjectRepository extends JpaRepository<EmployeeProject, Long> {
    List<EmployeeProject> findByEmployeeId(Long employeeId);
}