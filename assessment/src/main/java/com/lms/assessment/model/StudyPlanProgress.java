package com.lms.assessment.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "study_plan_progress",
    uniqueConstraints = @UniqueConstraint(columnNames = {"study_plan_item_id", "student_email"})
)
public class StudyPlanProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "study_plan_item_id", nullable = false)
    private Long studyPlanItemId;

    @Column(name = "study_plan_id", nullable = false)
    private Long studyPlanId;

    @Column(name = "student_email", nullable = false)
    private String studentEmail;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(name = "problem_id", nullable = false)
    private Long problemId;

    @Column(name = "completed", nullable = false)
    private boolean completed = false;

    @Column(name = "marks_obtained")
    private Integer marksObtained;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ── Getters & Setters ──────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStudyPlanItemId() { return studyPlanItemId; }
    public void setStudyPlanItemId(Long studyPlanItemId) { this.studyPlanItemId = studyPlanItemId; }

    public Long getStudyPlanId() { return studyPlanId; }
    public void setStudyPlanId(Long studyPlanId) { this.studyPlanId = studyPlanId; }

    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public Long getProblemId() { return problemId; }
    public void setProblemId(Long problemId) { this.problemId = problemId; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public Integer getMarksObtained() { return marksObtained; }
    public void setMarksObtained(Integer marksObtained) { this.marksObtained = marksObtained; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}