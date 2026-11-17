package com.ems.personal.controller;

import com.ems.personal.dto.PersonalDetailsRequest;
import com.ems.personal.dto.PersonalDetailsResponse;
import com.ems.personal.entity.PersonalDetails;
import com.ems.personal.service.PersonalDetailsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/personal-details")
public class PersonalDetailsController {

    private final PersonalDetailsService personalDetailsService;

    public PersonalDetailsController(PersonalDetailsService personalDetailsService) {
        this.personalDetailsService = personalDetailsService;
    }

    @PostMapping
    public ResponseEntity<PersonalDetailsResponse> saveOrUpdatePersonalDetails(
            @Valid @RequestBody PersonalDetailsRequest request) {
        PersonalDetails savedDetails = personalDetailsService.saveOrUpdatePersonalDetails(request);
        return ResponseEntity.ok(PersonalDetailsResponse.from(savedDetails));
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<PersonalDetailsResponse> getPersonalDetailsByEmployeeId(
            @PathVariable Long employeeId) {
        PersonalDetails personalDetails =
                personalDetailsService.getPersonalDetailsByEmployeeId(employeeId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(PersonalDetailsResponse.from(personalDetails));
    }
}
