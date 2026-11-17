package com.ems.personal.controller;

import com.ems.personal.entity.PersonalDetails;
import com.ems.personal.service.PersonalDetailsService;
import java.sql.SQLException;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PersonalDetailsControllerTest {

    private static final String VALID_REQUEST = """
            {
              "employeeId": 100001,
              "dateOfBirth": "1990-01-02",
              "gender": "female",
              "mobileNumber": "1234567890",
              "personalEmail": "employee@example.com",
              "emergencyContactName": "Alex Employee",
              "emergencyContactMobile": "2345678901",
              "currentAddressLine1": "1 Main Street",
              "currentAddressLine2": "",
              "currentCity": "Springfield",
              "currentPinCode": "123456",
              "permanentAddressLine1": "2 Main Street",
              "permanentAddressLine2": "",
              "permanentCity": "Springfield",
              "permanentPinCode": "654321"
            }
            """;

    private MockMvc mockMvc;
    private PersonalDetailsService personalDetailsService;

    @BeforeEach
    void setUp() {
        personalDetailsService = mock(PersonalDetailsService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new PersonalDetailsController(personalDetailsService))
                .setControllerAdvice(new PersonalDetailsExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void postPreservesTheExistingRequestAndResponseContract() throws Exception {
        PersonalDetails savedDetails = populatedDetails();
        when(personalDetailsService.saveOrUpdatePersonalDetails(any()))
                .thenReturn(savedDetails);

        mockMvc.perform(post("/api/personal-details")
                        .contentType(APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.employeeId").value(100001))
                .andExpect(jsonPath("$.dateOfBirth").value("1990-01-02"))
                .andExpect(jsonPath("$.currentAddressLine1").value("1 Main Street"));
    }

    @Test
    void getReturnsTheExistingResponseFieldNames() throws Exception {
        when(personalDetailsService.getPersonalDetailsByEmployeeId(100001L))
                .thenReturn(populatedDetails());

        mockMvc.perform(get("/api/personal-details/100001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(100001))
                .andExpect(jsonPath("$.personalEmail").value("employee@example.com"))
                .andExpect(jsonPath("$.permanentPinCode").value("654321"));
    }

    @Test
    void validationErrorsReturnAStableFieldErrorBody() throws Exception {
        String invalidRequest = VALID_REQUEST
                .replace("\"employeeId\": 100001", "\"employeeId\": 0")
                .replace("\"gender\": \"female\"", "\"gender\": \"unsupported\"")
                .replace("\"currentPinCode\": \"123456\"", "\"currentPinCode\": null");

        mockMvc.perform(post("/api/personal-details")
                        .contentType(APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed."))
                .andExpect(jsonPath("$.fieldErrors.employeeId").exists())
                .andExpect(jsonPath("$.fieldErrors.gender").exists())
                .andExpect(jsonPath("$.fieldErrors.currentPinCode").exists());
        verifyNoInteractions(personalDetailsService);
    }

    @Test
    void notFoundKeepsItsStatusAndReturnsTheStableErrorBody() throws Exception {
        when(personalDetailsService.getPersonalDetailsByEmployeeId(100001L))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Personal details not found."));

        mockMvc.perform(get("/api/personal-details/100001"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Personal details not found."));
    }

    @Test
    void duplicateEmployeeIdViolationsReturnConflict() throws Exception {
        when(personalDetailsService.saveOrUpdatePersonalDetails(any()))
                .thenThrow(new DataIntegrityViolationException(
                        "Duplicate employee id",
                        new SQLException(
                                "Unique constraint violation for employee_id in personal_details",
                                "23505",
                                1062)));

        mockMvc.perform(post("/api/personal-details")
                        .contentType(APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Personal details already exist for employee ID."));
    }

    private PersonalDetails populatedDetails() {
        PersonalDetails details = new PersonalDetails();
        details.setId(7L);
        details.setEmployeeId(100001L);
        details.setDateOfBirth(LocalDate.of(1990, 1, 2));
        details.setGender("female");
        details.setPersonalEmail("employee@example.com");
        details.setCurrentAddressLine1("1 Main Street");
        details.setPermanentPinCode("654321");
        return details;
    }
}
