package com.ems.personal.service;

import com.ems.personal.dto.PersonalDetailsRequest;
import com.ems.personal.entity.PersonalDetails;
import com.ems.personal.repository.PersonalDetailsRepository;
import java.sql.SQLException;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PersonalDetailsService {

    private static final String EMPLOYEE_ID_UNIQUE_CONSTRAINT =
            "uq_personal_details_employee_id";

    private final PersonalDetailsRepository personalDetailsRepository;

    public PersonalDetailsService(PersonalDetailsRepository personalDetailsRepository) {
        this.personalDetailsRepository = personalDetailsRepository;
    }

    @Transactional
    public PersonalDetails saveOrUpdatePersonalDetails(PersonalDetailsRequest request) {
        PersonalDetails personalDetails = personalDetailsRepository
                .findByEmployeeId(request.getEmployeeId())
                .orElseGet(PersonalDetails::new);

        personalDetails.setEmployeeId(request.getEmployeeId());
        personalDetails.setDateOfBirth(request.getDateOfBirth());
        personalDetails.setGender(request.getGender());
        personalDetails.setMobileNumber(request.getMobileNumber());
        personalDetails.setPersonalEmail(request.getPersonalEmail());
        personalDetails.setEmergencyContactName(request.getEmergencyContactName());
        personalDetails.setEmergencyContactMobile(request.getEmergencyContactMobile());
        personalDetails.setCurrentAddressLine1(request.getCurrentAddressLine1());
        personalDetails.setCurrentAddressLine2(request.getCurrentAddressLine2());
        personalDetails.setCurrentCity(request.getCurrentCity());
        personalDetails.setCurrentPinCode(request.getCurrentPinCode());
        personalDetails.setPermanentAddressLine1(request.getPermanentAddressLine1());
        personalDetails.setPermanentAddressLine2(request.getPermanentAddressLine2());
        personalDetails.setPermanentCity(request.getPermanentCity());
        personalDetails.setPermanentPinCode(request.getPermanentPinCode());

        try {
            return personalDetailsRepository.saveAndFlush(personalDetails);
        } catch (DataIntegrityViolationException exception) {
            if (isEmployeeIdDuplicate(exception)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Personal details already exist for employee ID: "
                                + request.getEmployeeId(),
                        exception);
            }
            throw exception;
        }
    }

    private boolean isEmployeeIdDuplicate(DataIntegrityViolationException exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            String message = cause.getMessage() == null
                    ? ""
                    : cause.getMessage().toLowerCase(Locale.ROOT);

            if (cause instanceof org.hibernate.exception.ConstraintViolationException violation) {
                String constraintName = violation.getConstraintName();
                if (isEmployeeIdConstraintName(constraintName)
                        || (constraintName == null && looksLikeEmployeeIdViolation(message))) {
                    return true;
                }
            }

            if (cause instanceof SQLException sqlException) {
                boolean duplicateSqlState = sqlException.getErrorCode() == 1062
                        || "23505".equals(sqlException.getSQLState())
                        || "23000".equals(sqlException.getSQLState());
                if (duplicateSqlState && looksLikeEmployeeIdViolation(sqlException.getMessage())) {
                    return true;
                }
            }

            if (looksLikeUniqueConstraintViolation(message)
                    && looksLikeEmployeeIdViolation(message)) {
                return true;
            }
        }
        return false;
    }

    private boolean isEmployeeIdConstraintName(String constraintName) {
        return constraintName != null
                && (constraintName.equalsIgnoreCase(EMPLOYEE_ID_UNIQUE_CONSTRAINT)
                        || constraintName.toLowerCase(Locale.ROOT).contains("employee_id")
                        || constraintName.toLowerCase(Locale.ROOT).contains("personal_details"));
    }

    private boolean looksLikeEmployeeIdViolation(String message) {
        if (message == null || message.isBlank()) {
            return false;
        }
        return message.contains("employee_id")
                || message.contains("personal_details")
                || message.contains(EMPLOYEE_ID_UNIQUE_CONSTRAINT.toLowerCase(Locale.ROOT));
    }

    private boolean looksLikeUniqueConstraintViolation(String message) {
        if (message == null || message.isBlank()) {
            return false;
        }
        return message.contains("duplicate")
                || message.contains("unique constraint")
                || message.contains("unique key")
                || message.contains("constraint violation")
                || message.contains("23505")
                || message.contains("23000");
    }

    @Transactional(readOnly = true)
    public PersonalDetails getPersonalDetailsByEmployeeId(Long employeeId) {
        return personalDetailsRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Personal details not found for employee ID: " + employeeId));
    }
}
