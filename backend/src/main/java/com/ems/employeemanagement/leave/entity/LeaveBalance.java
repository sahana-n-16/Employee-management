
package com.ems.employeemanagement.leave.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "leave_balances",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"employee_id", "leave_type", "leave_year"}
        )
    }
)
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long employeeId;

    private String leaveType;

    private Integer leaveYear;

    private Integer totalAllocated;

    private Integer used;

    private Integer remaining;

    // Default constructor
    public LeaveBalance() {
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    public Integer getLeaveYear() {
        return leaveYear;
    }

    public void setLeaveYear(Integer leaveYear) {
        this.leaveYear = leaveYear;
    }

    public Integer getTotalAllocated() {
        return totalAllocated;
    }

    public void setTotalAllocated(Integer totalAllocated) {
        this.totalAllocated = totalAllocated;
    }

    public Integer getUsed() {
        return used;
    }

    public void setUsed(Integer used) {
        this.used = used;
    }

    public Integer getRemaining() {
        return remaining;
    }

    public void setRemaining(Integer remaining) {
        this.remaining = remaining;
    }
}
