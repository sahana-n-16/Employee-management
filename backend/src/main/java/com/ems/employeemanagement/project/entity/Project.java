package com.ems.employeemanagement.project.entity;

import com.ems.employeemanagement.employee.entity.Employee;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "projects")
public class Project {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String projectCode;

    @Column(nullable = false)
    private String clientName;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reporting_manager_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // FIXES JSON ISSUE
    private Employee reportingManager;

    @Column(columnDefinition = "TEXT")
    private String tasks;

    @Column(columnDefinition = "TEXT")
    private String responsibilities;

    private Integer progress;

    public Project() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProjectCode() { return projectCode; }
    public void setProjectCode(String projectCode) { this.projectCode = projectCode; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Employee getReportingManager() { return reportingManager; }
    public void setReportingManager(Employee reportingManager) { this.reportingManager = reportingManager; }

    public String getTasks() { return tasks; }
    public void setTasks(String tasks) { this.tasks = tasks; }

    public String getResponsibilities() { return responsibilities; }
    public void setResponsibilities(String responsibilities) { this.responsibilities = responsibilities; }

    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }
}