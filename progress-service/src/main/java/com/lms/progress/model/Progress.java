package com.lms.progress.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "progress")
public class Progress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long courseId;
//fvfdf
    @ElementCollection
    @CollectionTable(name = "progress_completed_content", joinColumns = @JoinColumn(name = "progress_id"))
    @Column(name = "content_id")
    private List<Long> completedContentIds;

    private double progressPercentage;

    private Instant updatedAt;

    // -------------------- GETTERS --------------------
    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public List<Long> getCompletedContentIds() {
        return completedContentIds;
    }

    public double getProgressPercentage() {
        return progressPercentage;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // -------------------- SETTERS --------------------
    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public void setCompletedContentIds(List<Long> completedContentIds) {
        this.completedContentIds = completedContentIds;
    }

    public void setProgressPercentage(double progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
