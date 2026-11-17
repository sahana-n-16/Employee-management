package com.ems.employee_management_system;

import com.ems.personal.dto.PersonalDetailsRequest;
import com.ems.personal.service.PersonalDetailsService;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class EmployeeManagementSystemApplicationTests {

	@Autowired
	private RequestMappingHandlerMapping handlerMapping;

	@Autowired
	private PersonalDetailsService personalDetailsService;

	@Test
	void contextLoads() {
	}

	@Test
	void personalDetailsEndpointsAreRegistered() {
		var mappings = handlerMapping.getHandlerMethods().keySet();

		assertTrue(mappings.stream().anyMatch(mapping ->
				mapping.getPatternValues().contains("/api/personal-details")
						&& mapping.getMethodsCondition().getMethods().contains(RequestMethod.POST)));
		assertTrue(mappings.stream().anyMatch(mapping ->
				mapping.getPatternValues().contains("/api/personal-details/{employeeId}")
						&& mapping.getMethodsCondition().getMethods().contains(RequestMethod.GET)));
	}

    @Test
    @Transactional
    void savesUpdatesAndRetrievesPersonalDetails() {
        PersonalDetailsRequest request = validRequest();
        request.setEmployeeId(900001L);
        request.setPersonalEmail("first@example.com");

        var saved = personalDetailsService.saveOrUpdatePersonalDetails(request);
        request.setPersonalEmail("updated@example.com");
        var updated = personalDetailsService.saveOrUpdatePersonalDetails(request);
        var retrieved = personalDetailsService.getPersonalDetailsByEmployeeId(900001L);

        assertEquals(saved.getId(), updated.getId());
        assertEquals("updated@example.com", retrieved.getPersonalEmail());
    }

    private PersonalDetailsRequest validRequest() {
        PersonalDetailsRequest request = new PersonalDetailsRequest();
        request.setDateOfBirth(LocalDate.of(1990, 1, 2));
        request.setGender("female");
        request.setMobileNumber("1234567890");
        request.setPersonalEmail("employee@example.com");
        request.setEmergencyContactName("Alex Employee");
        request.setEmergencyContactMobile("2345678901");
        request.setCurrentAddressLine1("1 Main Street");
        request.setCurrentCity("Springfield");
        request.setCurrentPinCode("123456");
        request.setPermanentAddressLine1("2 Main Street");
        request.setPermanentCity("Springfield");
        request.setPermanentPinCode("654321");
        return request;
    }
}
