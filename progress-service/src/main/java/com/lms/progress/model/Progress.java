package com.lms.progress.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;

@Entity
@Table(
    name = "progress",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"studentEmail", "courseId"})
    }
)
public class Progress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ EMAIL BASED
    @Column(nullable = false)
    private String studentEmail;

    private Long courseId;

    @ElementCollection
    @CollectionTable(
        name = "progress_completed_content",
        joinColumns = @JoinColumn(name = "progress_id")
    )
    @Column(name = "content_id")
    private List<Long> completedContentIds;

    private double progressPercentage;

    private Instant updatedAt;

    // GETTERS
    public Long getId() { return id; }
    public String getStudentEmail() { return studentEmail; }
    public Long getCourseId() { return courseId; }
    public List<Long> getCompletedContentIds() { return completedContentIds; }
    public double getProgressPercentage() { return progressPercentage; }
    public Instant getUpdatedAt() { return updatedAt; }

    // SETTERS
    public void setId(Long id) { this.id = id; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public void setCompletedContentIds(List<Long> completedContentIds) {
        this.completedContentIds = completedContentIds;
    }
    public void setProgressPercentage(double progressPercentage) {
        this.progressPercentage = progressPercentage;
    }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}