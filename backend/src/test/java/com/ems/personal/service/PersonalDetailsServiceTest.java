package com.ems.personal.service;

import com.ems.personal.dto.PersonalDetailsRequest;
import com.ems.personal.entity.PersonalDetails;
import com.ems.personal.repository.PersonalDetailsRepository;
import java.sql.SQLException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PersonalDetailsServiceTest {

    private PersonalDetailsRepository repository;
    private PersonalDetailsService service;

    @BeforeEach
    void setUp() {
        repository = mock(PersonalDetailsRepository.class);
        service = new PersonalDetailsService(repository);
    }

    @Test
    void savesPersonalDetailsForEmployeeId() {
        PersonalDetailsRequest request = new PersonalDetailsRequest();
        request.setEmployeeId(100001L);
        request.setPersonalEmail("employee@example.com");
        when(repository.findByEmployeeId(100001L)).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any(PersonalDetails.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PersonalDetails saved = service.saveOrUpdatePersonalDetails(request);

        verify(repository).findByEmployeeId(100001L);
        verify(repository).saveAndFlush(any(PersonalDetails.class));
        assertEquals(100001L, saved.getEmployeeId());
        assertEquals("employee@example.com", saved.getPersonalEmail());
    }

    @Test
    void updatesExistingPersonalDetailsForEmployeeId() {
        PersonalDetails existing = new PersonalDetails();
        existing.setEmployeeId(100001L);
        existing.setPersonalEmail("old@example.com");

        PersonalDetailsRequest request = new PersonalDetailsRequest();
        request.setEmployeeId(100001L);
        request.setPersonalEmail("new@example.com");

        when(repository.findByEmployeeId(100001L)).thenReturn(Optional.of(existing));
        when(repository.saveAndFlush(any(PersonalDetails.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PersonalDetails saved = service.saveOrUpdatePersonalDetails(request);

        assertSame(existing, saved);
        assertEquals("new@example.com", saved.getPersonalEmail());
        verify(repository).saveAndFlush(existing);
    }

    @Test
    void retrievesPersonalDetailsByEmployeeId() {
        PersonalDetails expected = new PersonalDetails();
        expected.setEmployeeId(100001L);
        when(repository.findByEmployeeId(100001L)).thenReturn(Optional.of(expected));

        PersonalDetails actual = service.getPersonalDetailsByEmployeeId(100001L);

        verify(repository).findByEmployeeId(100001L);
        assertSame(expected, actual);
    }

    @Test
    void reportsNotFoundWhenEmployeeHasNoPersonalDetails() {
        when(repository.findByEmployeeId(100001L)).thenReturn(Optional.empty());

        var exception = assertThrows(ResponseStatusException.class,
                () -> service.getPersonalDetailsByEmployeeId(100001L));

        assertEquals(404, exception.getStatusCode().value());
    }

    @Test
    void reportsConflictWhenEmployeeIdViolatesUniqueConstraint() {
        PersonalDetailsRequest request = new PersonalDetailsRequest();
        request.setEmployeeId(100001L);
        when(repository.findByEmployeeId(100001L)).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any(PersonalDetails.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "Duplicate employee ID",
                        new SQLException(
                                "Duplicate entry for key 'uq_personal_details_employee_id'",
                                "23000",
                                1062)));

        var exception = assertThrows(
                ResponseStatusException.class,
                () -> service.saveOrUpdatePersonalDetails(request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void reportsConflictWhenUniqueConstraintHasNoExplicitConstraintName() {
        PersonalDetailsRequest request = new PersonalDetailsRequest();
        request.setEmployeeId(100001L);
        when(repository.findByEmployeeId(100001L)).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any(PersonalDetails.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "Data integrity violation",
                        new SQLException(
                                "Unique constraint violation for employee_id in personal_details",
                                "23505",
                                1062)));

        var exception = assertThrows(
                ResponseStatusException.class,
                () -> service.saveOrUpdatePersonalDetails(request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }
}
