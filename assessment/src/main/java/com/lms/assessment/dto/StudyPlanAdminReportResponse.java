package com.lms.assessment.dto;

import java.time.LocalDateTime;

public class StudyPlanAdminReportResponse {

    private Long id;
    private String title;
    private String trainerEmail;
    private Long batchId;
    private boolean active;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private int itemCount;

    // Nullable — only populated (with "Standalone" fallback) for the /superadmin endpoint
    private String organizationId;

    public StudyPlanAdminReportResponse() {
    }

    public StudyPlanAdminReportResponse(Long id, String title, String trainerEmail, Long batchId,
                                         boolean active, LocalDateTime dueDate, LocalDateTime createdAt,
                                         int itemCount, String organizationId) {
        this.id = id;
        this.title = title;
        this.trainerEmail = trainerEmail;
        this.batchId = batchId;
        this.active = active;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.itemCount = itemCount;
        this.organizationId = organizationId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTrainerEmail() { return trainerEmail; }
    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getItemCount() { return itemCount; }
    public void setItemCount(int itemCount) { this.itemCount = itemCount; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
}