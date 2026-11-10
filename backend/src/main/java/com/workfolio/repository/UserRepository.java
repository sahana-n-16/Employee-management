package com.workfolio.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.workfolio.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByCompanyEmail(String companyEmail);

    Optional<User> findByEmploymentId(String employmentId);

    boolean existsByCompanyEmail(String companyEmail);

    boolean existsByEmploymentId(String employmentId);
}
