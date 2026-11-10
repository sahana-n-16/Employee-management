package com.workfolio.dto;

import com.workfolio.enums.Role;

public class LoginResponse {

    private Long id;
    private String fullName;
    private String companyEmail;
    private String employmentId;
    private Role role;
    private String token;

    public LoginResponse(Long id, String fullName,
                         String companyEmail,
                         String employmentId,
                         Role role, String token) {

        this.id = id;
        this.fullName = fullName;
        this.companyEmail = companyEmail;
        this.employmentId = employmentId;
        this.role = role;
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getCompanyEmail() {
        return companyEmail;
    }

    public String getEmploymentId() {
        return employmentId;
    }

    public Role getRole() {
        return role;
    }

    public String getToken() {
        return token;
    }
}