package com.ems.employee_management_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
    "com.ems.employee_management_system",
    "com.ems.personal",
    "com.ems.attendance"
})
@EntityScan(basePackages = {
    "com.ems.personal.entity",
    "com.ems.attendance.entity"
})
@EnableJpaRepositories(basePackages = {
    "com.ems.personal.repository",
    "com.ems.attendance.repository"
})
public class EmployeeManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmployeeManagementSystemApplication.class, args);
    }
}