package com.ems.employeemanagement.project.controller;

import com.ems.employeemanagement.project.dto.ProjectRequest;
import com.ems.employeemanagement.project.entity.Project;
import com.ems.employeemanagement.project.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "http://localhost:5173")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @PostMapping("/employee/{employeeId}")
    public ResponseEntity<?> addProject(@PathVariable Long employeeId, @RequestBody ProjectRequest request) {
        try {
            request.setEmployeeId(employeeId);
            String response = projectService.addProjectToEmployee(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Send a 400 Bad Request with the exact error message (e.g. "Duplicate Project Code")
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<Project>> getProjects(@PathVariable Long employeeId) {
        return ResponseEntity.ok(projectService.getEmployeeProjects(employeeId));
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<String> updateProject(@PathVariable Long projectId, @RequestBody ProjectRequest request) {
        projectService.updateProject(projectId, request);
        return ResponseEntity.ok("Project updated successfully!");
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<String> deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.ok("Project deleted successfully!");
    }
}