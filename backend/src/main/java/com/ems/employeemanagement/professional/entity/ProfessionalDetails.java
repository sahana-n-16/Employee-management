
package com.ems.employeemanagement.professional.entity;

import java.time.LocalDate;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "professional_details")
public class ProfessionalDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long employeeId;

    @NotBlank(message = "Employment Code is required")
    @Column(unique = true, nullable = false)
    private String employmentCode;

    @NotBlank(message = "Company Email is required")
    @Email(message = "Enter a valid company email")
    @Column(unique = true, nullable = false)
    private String companyEmail;

    @NotBlank(message = "Office Phone is required")
    @Pattern(
        regexp = "\\d{8,12}",
        message = "Office Phone must contain 8 to 12 digits"
    )
    private String officePhone;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "PIN Code is required")
    @Pattern(
        regexp = "\\d{6}",
        message = "PIN Code must contain exactly 6 digits"
    )
    private String pinCode;

    @NotBlank(message = "Office Address is required")
    private String officeAddress;

    @NotBlank(message = "Reporting Manager is required")
    private String reportingManager;

    @NotBlank(message = "HR Name is required")
    private String hrName;

    @NotNull(message = "Date of Joining is required")
    private LocalDate dateOfJoining;
    
    @NotBlank(message = "Designation is required")
    private String designation;

    @NotBlank(message = "Department is required")
    private String department;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmploymentCode() {
        return employmentCode;
    }

    public void setEmploymentCode(String employmentCode) {
        this.employmentCode = employmentCode;
    }

    public String getCompanyEmail() {
        return companyEmail;
    }

    public void setCompanyEmail(String companyEmail) {
        this.companyEmail = companyEmail;
    }

    public String getOfficePhone() {
        return officePhone;
    }

    public void setOfficePhone(String officePhone) {
        this.officePhone = officePhone;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPinCode() {
        return pinCode;
    }

    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }

    public String getOfficeAddress() {
        return officeAddress;
    }

    public void setOfficeAddress(String officeAddress) {
        this.officeAddress = officeAddress;
    }

    public String getReportingManager() {
        return reportingManager;
    }

    public void setReportingManager(String reportingManager) {
        this.reportingManager = reportingManager;
    }

    public String getHrName() {
        return hrName;
    }

    public void setHrName(String hrName) {
        this.hrName = hrName;
    }

    public LocalDate getDateOfJoining() {
        return dateOfJoining;
    }

    public void setDateOfJoining(LocalDate dateOfJoining) {
        this.dateOfJoining = dateOfJoining;
    }
    
    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
