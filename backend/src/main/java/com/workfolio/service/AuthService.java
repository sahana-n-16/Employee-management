package com.workfolio.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.workfolio.config.JwtService;
import com.workfolio.dto.LoginRequest;
import com.workfolio.dto.LoginResponse;
import com.workfolio.dto.RegisterRequest;
import com.workfolio.entity.User;
import com.workfolio.enums.Role;
import com.workfolio.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // Employee Registration
    public String register(RegisterRequest request) {

        if (userRepository.existsByCompanyEmail(request.getCompanyEmail())) {
            throw new IllegalArgumentException("Company email already exists");
        }

        if (userRepository.existsByEmploymentId(request.getEmploymentId())) {
            throw new IllegalArgumentException("Employment ID already exists");
        }

        User user = new User();

        user.setFullName(request.getFullName());
        user.setCompanyEmail(request.getCompanyEmail());
        user.setEmploymentId(request.getEmploymentId());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.EMPLOYEE);

        userRepository.save(user);

        return "Employee registered successfully";
    }

    // Employee Login
    public LoginResponse login(LoginRequest request) {

        String identifier = request.getIdentifier().trim();

        Optional<User> optionalUser;

        if (identifier.contains("@")) {
            optionalUser = userRepository.findByCompanyEmail(identifier);
        } else {
            optionalUser = userRepository.findByEmploymentId(identifier);
        }

        User user = optionalUser.orElseThrow(() ->
                new IllegalArgumentException("Invalid credentials"));

        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

        if (!passwordMatches) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        // Generate JWT token after successful login
        String token = jwtService.generateToken(user.getCompanyEmail());

        return new LoginResponse(
                user.getId(),
                user.getFullName(),
                user.getCompanyEmail(),
                user.getEmploymentId(),
                user.getRole(),
                token
        );
    }
}