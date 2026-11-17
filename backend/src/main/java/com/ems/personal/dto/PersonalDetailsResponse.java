package com.ems.personal.dto;

import com.ems.personal.entity.PersonalDetails;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PersonalDetailsResponse(
        Long id,
        Long employeeId,
        LocalDate dateOfBirth,
        String gender,
        String mobileNumber,
        String personalEmail,
        String emergencyContactName,
        String emergencyContactMobile,
        String currentAddressLine1,
        String currentAddressLine2,
        String currentCity,
        String currentPinCode,
        String permanentAddressLine1,
        String permanentAddressLine2,
        String permanentCity,
        String permanentPinCode,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static PersonalDetailsResponse from(PersonalDetails details) {
        return new PersonalDetailsResponse(
                details.getId(),
                details.getEmployeeId(),
                details.getDateOfBirth(),
                details.getGender(),
                details.getMobileNumber(),
                details.getPersonalEmail(),
                details.getEmergencyContactName(),
                details.getEmergencyContactMobile(),
                details.getCurrentAddressLine1(),
                details.getCurrentAddressLine2(),
                details.getCurrentCity(),
                details.getCurrentPinCode(),
                details.getPermanentAddressLine1(),
                details.getPermanentAddressLine2(),
                details.getPermanentCity(),
                details.getPermanentPinCode(),
                details.getCreatedAt(),
                details.getUpdatedAt());
    }
}
