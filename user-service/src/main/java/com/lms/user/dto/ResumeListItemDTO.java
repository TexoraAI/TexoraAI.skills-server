package com.lms.user.dto;

import java.time.LocalDateTime;

/**
 * WHY: Resume list page only needs summary data — not all 5 child collections.
 * Loading full ResumeResponseDTO with all collections for a list of 20 resumes
 * triggers 100+ DB queries. This projection carries only what the card UI needs.
 */
public class ResumeListItemDTO {

    private Long id;
    private String title;
    private String templateName;
    private Integer resumeScore;
    private Boolean isAtsFriendly;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;
    // WHY: Job title shown on resume card in list view
    private String jobTitle;

    public ResumeListItemDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }
    public Integer getResumeScore() { return resumeScore; }
    public void setResumeScore(Integer resumeScore) { this.resumeScore = resumeScore; }
    public Boolean getIsAtsFriendly() { return isAtsFriendly; }
    public void setIsAtsFriendly(Boolean isAtsFriendly) { this.isAtsFriendly = isAtsFriendly; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
}