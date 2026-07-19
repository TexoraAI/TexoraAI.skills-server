package com.lms.assessment.dto;

import com.lms.assessment.model.CodingProblem;

import java.time.LocalDateTime;

public class CodingProblemAdminReportResponse {

    private Long id;
    private String title;
    private String trainerEmail;
    private CodingProblem.Difficulty difficulty;
    private Integer totalMarks;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private long assignedBatchCount;

    // Nullable — only populated (with "Standalone" fallback) for the /superadmin endpoint
    private String organizationId;

    public CodingProblemAdminReportResponse() {
    }

    public CodingProblemAdminReportResponse(Long id, String title, String trainerEmail,
                                             CodingProblem.Difficulty difficulty, Integer totalMarks,
                                             Boolean isActive, LocalDateTime createdAt,
                                             long assignedBatchCount, String organizationId) {
        this.id = id;
        this.title = title;
        this.trainerEmail = trainerEmail;
        this.difficulty = difficulty;
        this.totalMarks = totalMarks;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.assignedBatchCount = assignedBatchCount;
        this.organizationId = organizationId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTrainerEmail() { return trainerEmail; }
    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }

    public CodingProblem.Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(CodingProblem.Difficulty difficulty) { this.difficulty = difficulty; }

    public Integer getTotalMarks() { return totalMarks; }
    public void setTotalMarks(Integer totalMarks) { this.totalMarks = totalMarks; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public long getAssignedBatchCount() { return assignedBatchCount; }
    public void setAssignedBatchCount(long assignedBatchCount) { this.assignedBatchCount = assignedBatchCount; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
}