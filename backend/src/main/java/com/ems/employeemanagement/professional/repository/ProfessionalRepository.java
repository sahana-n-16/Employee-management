package com.ems.employeemanagement.professional.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ems.employeemanagement.professional.entity.ProfessionalDetails;

@Repository
public interface ProfessionalRepository
        extends JpaRepository<ProfessionalDetails, Long> {

    Optional<ProfessionalDetails> findByEmployeeId(Long employeeId);

}