
package com.ems.employeemanagement.finance.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ems.employeemanagement.finance.entity.FinanceDetails;
import com.ems.employeemanagement.finance.repository.FinanceDetailsRepository;

@Service
@Transactional
public class FinanceDetailsService {

    private final FinanceDetailsRepository financeDetailsRepository;

    public FinanceDetailsService(
            FinanceDetailsRepository financeDetailsRepository) {
        this.financeDetailsRepository = financeDetailsRepository;
    }

    // Get financial details using Employee ID
    @Transactional(readOnly = true)
    public FinanceDetails getFinanceDetails(Long employeeId) {

        return financeDetailsRepository.findByEmployeeId(employeeId)
                .orElseThrow(() ->
                    new RuntimeException(
                        "Finance details not found for Employee ID: "
                        + employeeId
                    )
                );
    }

    // Create financial details
    public FinanceDetails saveFinanceDetails(FinanceDetails details) {

        if (financeDetailsRepository
                .findByEmployeeId(details.getEmployeeId()).isPresent()) {
            throw new IllegalArgumentException(
                "Finance details already exist for this employee"
            );
        }

        if (financeDetailsRepository
                .existsByPanNumber(details.getPanNumber())) {
            throw new IllegalArgumentException(
                "PAN number already exists"
            );
        }

        if (financeDetailsRepository
                .existsByAadhaarNumber(details.getAadhaarNumber())) {
            throw new IllegalArgumentException(
                "Aadhaar number already exists"
            );
        }

        return financeDetailsRepository.save(details);
    }

    // Update financial details
    public FinanceDetails updateFinanceDetails(
            Long employeeId, FinanceDetails details) {

        FinanceDetails existing = getFinanceDetails(employeeId);

        if (!existing.getPanNumber().equals(details.getPanNumber())
                && financeDetailsRepository
                    .existsByPanNumber(details.getPanNumber())) {
            throw new IllegalArgumentException(
                "PAN number already exists"
            );
        }

        if (!existing.getAadhaarNumber().equals(details.getAadhaarNumber())
                && financeDetailsRepository
                    .existsByAadhaarNumber(details.getAadhaarNumber())) {
            throw new IllegalArgumentException(
                "Aadhaar number already exists"
            );
        }

        existing.setPanNumber(details.getPanNumber());
        existing.setAadhaarNumber(details.getAadhaarNumber());
        existing.setBankName(details.getBankName());
        existing.setBankBranch(details.getBankBranch());
        existing.setAccountNumber(details.getAccountNumber());
        existing.setIfscCode(details.getIfscCode());

        return financeDetailsRepository.save(existing);
    }
}
