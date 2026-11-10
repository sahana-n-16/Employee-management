
package com.workfolio.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoleTestController {

    @GetMapping("/api/admin/test")
    public String adminTest() {
        return "Welcome Admin! You can access Admin resources.";
    }

    @GetMapping("/api/employee/test")
    public String employeeTest() {
        return "Welcome! You can access Employee resources.";
    }
}
