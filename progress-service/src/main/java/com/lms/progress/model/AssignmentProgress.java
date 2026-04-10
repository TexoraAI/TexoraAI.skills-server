package com.lms.progress.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "assignment_progress")
public class AssignmentProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String studentEmail;
    private Long batchId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "completed_assignment_ids",
            joinColumns = @JoinColumn(name = "assignment_progress_id"))
    @Column(name = "assignment_id")
    private List<Long> completedAssignmentIds;

    private int totalAssignments;
    private double percentage;
    private Instant updatedAt;

    // Getters
    public Long getId() {
        return id;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public Long getBatchId() {
        return batchId;
    }

    public List<Long> getCompletedAssignmentIds() {
        return completedAssignmentIds;
    }

    public int getTotalAssignments() {
        return totalAssignments;
    }

    public double getPercentage() {
        return percentage;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public void setCompletedAssignmentIds(List<Long> completedAssignmentIds) {
        this.completedAssignmentIds = completedAssignmentIds;
    }

    public void setTotalAssignments(int totalAssignments) {
        this.totalAssignments = totalAssignments;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}