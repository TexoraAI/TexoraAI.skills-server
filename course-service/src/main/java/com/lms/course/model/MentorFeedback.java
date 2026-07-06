package com.lms.course.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "mentor_feedback")
public class MentorFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "candidate_name", nullable = false, length = 150)
    private String candidateName;

    @Column(name = "designation", nullable = false, length = 150)
    private String designation;

    @Column(name = "company", nullable = false, length = 150)
    private String company;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "feedback_message", nullable = false, columnDefinition = "TEXT")
    private String feedbackMessage;

//    @Column(name = "profile_image")
//    private String profileImage;
    @Column(name = "profile_image", columnDefinition = "TEXT")
    private String profileImage;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FeedbackStatus status = FeedbackStatus.ACTIVE;

    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;

    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public enum FeedbackStatus {
        ACTIVE,
        INACTIVE
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public MentorFeedback() {
    }

    // ── Getters & Setters ──────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getFeedbackMessage() {
        return feedbackMessage;
    }

    public void setFeedbackMessage(String feedbackMessage) {
        this.feedbackMessage = feedbackMessage;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public FeedbackStatus getStatus() {
        return status;
    }

    public void setStatus(FeedbackStatus status) {
        this.status = status;
    }

    public Boolean getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(Boolean isFeatured) {
        this.isFeatured = isFeatured;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}