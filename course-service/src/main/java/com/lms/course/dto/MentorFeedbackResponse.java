package com.lms.course.dto;

import java.time.Instant;

public class MentorFeedbackResponse {

    private Long id;
    private String candidateName;
    private String designation;
    private String company;
    private Integer rating;
    private String feedbackMessage;
    private String profileImage;
    private String status;
    private Boolean isFeatured;
    private Instant createdAt;
    private Instant updatedAt;

    public MentorFeedbackResponse() {
    }

    public MentorFeedbackResponse(Long id, String candidateName, String designation, String company,
                                   Integer rating, String feedbackMessage, String profileImage,
                                   String status, Boolean isFeatured, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.candidateName = candidateName;
        this.designation = designation;
        this.company = company;
        this.rating = rating;
        this.feedbackMessage = feedbackMessage;
        this.profileImage = profileImage;
        this.status = status;
        this.isFeatured = isFeatured;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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