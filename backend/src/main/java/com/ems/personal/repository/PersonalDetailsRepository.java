package com.ems.personal.repository;

import com.ems.personal.entity.PersonalDetails;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonalDetailsRepository extends JpaRepository<PersonalDetails, Long> {

    Optional<PersonalDetails> findByEmployeeId(Long employeeId);
}
