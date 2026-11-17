package com.ems.personal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class PersonalDetailsRequest {

    @NotNull(message = "Employee ID is required.")
    @Positive(message = "Employee ID must be positive.")
    private Long employeeId;

    @NotNull(message = "Date of birth is required.")
    @PastOrPresent(message = "Date of birth cannot be in the future.")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Gender is required.")
    @Pattern(
            regexp = "^(female|male|non-binary|prefer-not-to-say)$",
            message = "Gender must be one of the supported values.")
    private String gender;

    @NotBlank(message = "Mobile number is required.")
    @Pattern(regexp = "^\\d{10}$", message = "Mobile number must contain exactly 10 digits.")
    @Size(max = 20, message = "Mobile number must not exceed 20 characters.")
    private String mobileNumber;

    @NotBlank(message = "Personal email is required.")
    @Email(message = "Personal email must be a valid email address.")
    @Size(max = 254, message = "Personal email must not exceed 254 characters.")
    private String personalEmail;

    @NotBlank(message = "Emergency contact name is required.")
    @Size(max = 150, message = "Emergency contact name must not exceed 150 characters.")
    private String emergencyContactName;

    @NotBlank(message = "Emergency contact mobile number is required.")
    @Pattern(regexp = "^\\d{10}$", message = "Emergency contact mobile number must contain exactly 10 digits.")
    @Size(max = 20, message = "Emergency contact mobile number must not exceed 20 characters.")
    private String emergencyContactMobile;

    @NotBlank(message = "Current address line 1 is required.")
    @Size(max = 255, message = "Current address line 1 must not exceed 255 characters.")
    private String currentAddressLine1;

    @Size(max = 255, message = "Current address line 2 must not exceed 255 characters.")
    private String currentAddressLine2;

    @NotBlank(message = "Current city is required.")
    @Size(max = 100, message = "Current city must not exceed 100 characters.")
    private String currentCity;

    @NotBlank(message = "Current PIN code is required.")
    @Pattern(regexp = "^\\d{6}$", message = "Current PIN code must contain exactly 6 digits.")
    private String currentPinCode;

    @NotBlank(message = "Permanent address line 1 is required.")
    @Size(max = 255, message = "Permanent address line 1 must not exceed 255 characters.")
    private String permanentAddressLine1;

    @Size(max = 255, message = "Permanent address line 2 must not exceed 255 characters.")
    private String permanentAddressLine2;

    @NotBlank(message = "Permanent city is required.")
    @Size(max = 100, message = "Permanent city must not exceed 100 characters.")
    private String permanentCity;

    @NotBlank(message = "Permanent PIN code is required.")
    @Pattern(regexp = "^\\d{6}$", message = "Permanent PIN code must contain exactly 6 digits.")
    private String permanentPinCode;

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getPersonalEmail() {
        return personalEmail;
    }

    public void setPersonalEmail(String personalEmail) {
        this.personalEmail = personalEmail;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getEmergencyContactMobile() {
        return emergencyContactMobile;
    }

    public void setEmergencyContactMobile(String emergencyContactMobile) {
        this.emergencyContactMobile = emergencyContactMobile;
    }

    public String getCurrentAddressLine1() {
        return currentAddressLine1;
    }

    public void setCurrentAddressLine1(String currentAddressLine1) {
        this.currentAddressLine1 = currentAddressLine1;
    }

    public String getCurrentAddressLine2() {
        return currentAddressLine2;
    }

    public void setCurrentAddressLine2(String currentAddressLine2) {
        this.currentAddressLine2 = currentAddressLine2;
    }

    public String getCurrentCity() {
        return currentCity;
    }

    public void setCurrentCity(String currentCity) {
        this.currentCity = currentCity;
    }

    public String getCurrentPinCode() {
        return currentPinCode;
    }

    public void setCurrentPinCode(String currentPinCode) {
        this.currentPinCode = currentPinCode;
    }

    public String getPermanentAddressLine1() {
        return permanentAddressLine1;
    }

    public void setPermanentAddressLine1(String permanentAddressLine1) {
        this.permanentAddressLine1 = permanentAddressLine1;
    }

    public String getPermanentAddressLine2() {
        return permanentAddressLine2;
    }

    public void setPermanentAddressLine2(String permanentAddressLine2) {
        this.permanentAddressLine2 = permanentAddressLine2;
    }

    public String getPermanentCity() {
        return permanentCity;
    }

    public void setPermanentCity(String permanentCity) {
        this.permanentCity = permanentCity;
    }

    public String getPermanentPinCode() {
        return permanentPinCode;
    }

    public void setPermanentPinCode(String permanentPinCode) {
        this.permanentPinCode = permanentPinCode;
    }
}
