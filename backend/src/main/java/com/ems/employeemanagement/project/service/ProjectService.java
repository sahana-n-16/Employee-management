package com.ems.employeemanagement.project.service;

import com.ems.employeemanagement.employee.entity.Employee;
import com.ems.employeemanagement.employee.repository.EmployeeRepository;
import com.ems.employeemanagement.project.dto.ProjectRequest;
import com.ems.employeemanagement.project.entity.EmployeeProject;
import com.ems.employeemanagement.project.entity.Project;
import com.ems.employeemanagement.project.repository.EmployeeProjectRepository;
import com.ems.employeemanagement.project.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepo;

    @Autowired
    private EmployeeProjectRepository empProjRepo;

    @Autowired
    private EmployeeRepository employeeRepo;

    public String addProjectToEmployee(ProjectRequest req) {
        Employee employee = employeeRepo.findById(req.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (projectRepo.findByProjectCode(req.getProjectCode()).isPresent()) {
            throw new RuntimeException("Duplicate Project Code");
        }

        Project project = new Project();
        project.setProjectCode(req.getProjectCode());
        project.setClientName(req.getClientName());
        project.setStartDate(req.getStartDate());
        project.setEndDate(req.getEndDate());
        
        if (req.getReportingManagerId() != null) {
            Employee manager = employeeRepo.findById(req.getReportingManagerId())
                    .orElseThrow(() -> new RuntimeException("Reporting Manager not found"));
            project.setReportingManager(manager);
        }

        project.setTasks(req.getTasks());
        project.setResponsibilities(req.getResponsibilities());
        project.setProgress(req.getProgress());
        
        projectRepo.save(project);

        EmployeeProject mapping = new EmployeeProject();
        mapping.setEmployee(employee);
        mapping.setProject(project);
        empProjRepo.save(mapping);

        return "Project assigned successfully!";
    }

    public List<Project> getEmployeeProjects(Long employeeId) {
        return empProjRepo.findByEmployeeId(employeeId).stream()
                .map(EmployeeProject::getProject)
                .collect(Collectors.toList());
    }

    public void updateProject(Long projectId, ProjectRequest req) {
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        
        project.setProjectCode(req.getProjectCode());
        project.setClientName(req.getClientName());
        project.setStartDate(req.getStartDate());
        project.setEndDate(req.getEndDate());
        
        if (req.getReportingManagerId() != null) {
            Employee manager = employeeRepo.findById(req.getReportingManagerId())
                    .orElseThrow(() -> new RuntimeException("Reporting Manager not found"));
            project.setReportingManager(manager);
        }

        project.setTasks(req.getTasks());
        project.setResponsibilities(req.getResponsibilities());
        project.setProgress(req.getProgress());
        
        projectRepo.save(project);
    }

    public void deleteProject(Long projectId) {
        List<EmployeeProject> mappings = empProjRepo.findAll().stream()
                .filter(m -> m.getProject().getId().equals(projectId))
                .collect(Collectors.toList());
        empProjRepo.deleteAll(mappings);
        projectRepo.deleteById(projectId);
    }
}