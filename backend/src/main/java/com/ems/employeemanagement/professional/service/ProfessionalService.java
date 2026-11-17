
package com.ems.employeemanagement.professional.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ems.employeemanagement.professional.entity.EmploymentHistory;
import com.ems.employeemanagement.professional.entity.ProfessionalDetails;
import com.ems.employeemanagement.professional.repository.EmploymentHistoryRepository;
import com.ems.employeemanagement.professional.repository.ProfessionalRepository;

@Service
public class ProfessionalService {

    private final ProfessionalRepository professionalRepository;
    private final EmploymentHistoryRepository employmentHistoryRepository;

    public ProfessionalService(
            ProfessionalRepository professionalRepository,
            EmploymentHistoryRepository employmentHistoryRepository) {

        this.professionalRepository = professionalRepository;
        this.employmentHistoryRepository = employmentHistoryRepository;
    }

    // Get professional details using employee ID
    public ProfessionalDetails getProfessionalDetails(Long employeeId) {

        return professionalRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException(
                        "Professional details not found for employee ID: " + employeeId));
    }

    // Save or update professional details
    public ProfessionalDetails saveProfessionalDetails(ProfessionalDetails details) {

        return professionalRepository.save(details);
    }

    // Add previous employment record
    @Transactional
    public EmploymentHistory addEmploymentHistory(
            Long employeeId, EmploymentHistory history) {

        // Validate employment dates
        if (history.getJoiningDate() == null || history.getEndDate() == null) {
            throw new IllegalArgumentException(
                    "Joining date and end date are required.");
        }

        if (history.getEndDate().isBefore(history.getJoiningDate())) {
            throw new IllegalArgumentException(
                    "End date cannot be before joining date.");
        }

        if (history.getCompanyName() == null ||
                history.getCompanyName().isBlank()) {
            throw new IllegalArgumentException(
                    "Company name is required.");
        }

        ProfessionalDetails professionalDetails =
                getProfessionalDetails(employeeId);

        history.setProfessionalDetails(professionalDetails);

        return employmentHistoryRepository.save(history);
    }

    // Get all employment history records
    public List<EmploymentHistory> getEmploymentHistory(Long employeeId) {

        ProfessionalDetails professionalDetails =
                getProfessionalDetails(employeeId);

        return employmentHistoryRepository
                .findByProfessionalDetails(professionalDetails);
    }

    // Delete a previous employment record
    @Transactional
    public void deleteEmploymentHistory(Long employeeId, Long historyId) {

        // Find the employment record
        EmploymentHistory history = employmentHistoryRepository.findById(historyId)
                .orElseThrow(() -> new RuntimeException(
                        "Employment history not found with ID: " + historyId));

        // Verify that the record belongs to the requested employee
        Long recordEmployeeId = history.getProfessionalDetails().getEmployeeId();

        if (!recordEmployeeId.equals(employeeId)) {
            throw new IllegalArgumentException(
                    "This employment record does not belong to employee ID: "
                            + employeeId);
        }

        // Delete the record
        employmentHistoryRepository.delete(history);
    }
}
