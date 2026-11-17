
package com.ems.employeemanagement.finance.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ems.employeemanagement.finance.entity.FinanceDetails;

@Repository
public interface FinanceDetailsRepository
        extends JpaRepository<FinanceDetails, Long> {

    Optional<FinanceDetails> findByEmployeeId(Long employeeId);

    boolean existsByPanNumber(String panNumber);

    boolean existsByAadhaarNumber(String aadhaarNumber);
}
