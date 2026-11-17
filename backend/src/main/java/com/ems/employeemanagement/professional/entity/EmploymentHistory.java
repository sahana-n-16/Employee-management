
package com.ems.employeemanagement.professional.entity;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

@Entity
@Table(name = "employment_history")
public class EmploymentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Company Name is required")
    private String companyName;

    @NotNull(message = "Joining Date is required")
    @PastOrPresent(message = "Joining Date cannot be in the future")
    private LocalDate joiningDate;

    @NotNull(message = "End Date is required")
    @PastOrPresent(message = "End Date cannot be in the future")
    private LocalDate endDate;

    @ManyToOne
    @JoinColumn(name = "professional_details_id", nullable = false)
    private ProfessionalDetails professionalDetails;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public LocalDate getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(LocalDate joiningDate) {
        this.joiningDate = joiningDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public ProfessionalDetails getProfessionalDetails() {
        return professionalDetails;
    }

    public void setProfessionalDetails(ProfessionalDetails professionalDetails) {
        this.professionalDetails = professionalDetails;
    }
}
