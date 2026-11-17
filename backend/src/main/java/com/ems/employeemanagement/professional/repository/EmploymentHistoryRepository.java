
package com.ems.employeemanagement.professional.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ems.employeemanagement.professional.entity.EmploymentHistory;
import com.ems.employeemanagement.professional.entity.ProfessionalDetails;

@Repository
public interface EmploymentHistoryRepository
        extends JpaRepository<EmploymentHistory, Long> {

    List<EmploymentHistory> findByProfessionalDetails(
            ProfessionalDetails professionalDetails);

}
