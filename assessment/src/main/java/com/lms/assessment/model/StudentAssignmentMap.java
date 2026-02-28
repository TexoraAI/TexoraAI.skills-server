package com.lms.assessment.model; // Adjust the package name as needed

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_assignment_map")
public class StudentAssignmentMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long assignmentId;

    private String studentEmail;

    private LocalDateTime assignedAt = LocalDateTime.now();

    // Default Constructor
    public StudentAssignmentMap() {}

    // Parameterized Constructor
    public StudentAssignmentMap(Long assignmentId, String studentEmail) {
        this.assignmentId = assignmentId;
        this.studentEmail = studentEmail;
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
}