
package com.ems.employeemanagement.finance.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "finance_details")
public class FinanceDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Temporary employee reference.
    // Employee entity
    @Column(unique = true, nullable = false)
    private Long employeeId;

    @NotBlank(message = "PAN number is required")
    @Pattern(
        regexp = "^[A-Z]{5}[0-9]{4}[A-Z]$",
        message = "Enter a valid PAN number"
    )
    @Column(nullable = false, unique = true)
    private String panNumber;

    @NotBlank(message = "Aadhaar number is required")
    @Pattern(
        regexp = "^[0-9]{12}$",
        message = "Aadhaar number must contain exactly 12 digits"
    )
    @Column(nullable = false, unique = true)
    private String aadhaarNumber;

    @NotBlank(message = "Bank name is required")
    private String bankName;

    @NotBlank(message = "Bank branch is required")
    private String bankBranch;

    @NotBlank(message = "Account number is required")
    @Pattern(
        regexp = "^[0-9]{9,18}$",
        message = "Account number must contain 9 to 18 digits"
    )
    @Column(nullable = false)
    private String accountNumber;

    @NotBlank(message = "IFSC code is required")
    @Pattern(
        regexp = "^[A-Z]{4}0[A-Z0-9]{6}$",
        message = "Enter a valid IFSC code"
    )
    @Column(nullable = false)
    private String ifscCode;

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

    public String getPanNumber() {
        return panNumber;
    }

    public void setPanNumber(String panNumber) {
        this.panNumber = panNumber;
    }

    public String getAadhaarNumber() {
        return aadhaarNumber;
    }

    public void setAadhaarNumber(String aadhaarNumber) {
        this.aadhaarNumber = aadhaarNumber;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankBranch() {
        return bankBranch;
    }

    public void setBankBranch(String bankBranch) {
        this.bankBranch = bankBranch;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode;
    }
}
