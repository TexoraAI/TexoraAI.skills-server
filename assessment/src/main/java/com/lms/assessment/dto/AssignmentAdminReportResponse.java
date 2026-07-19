package com.lms.assessment.dto;

import java.time.LocalDateTime;

public class AssignmentAdminReportResponse {

    private Long id;
    private String title;
    private String trainerEmail;
    private Long batchId;
    private LocalDateTime deadline;
    private Integer maxMarks;
    private LocalDateTime createdAt;
    private long submissionCount;

    // Nullable — only populated (with "Standalone" fallback) for the /superadmin endpoint
    private String organizationId;

    public AssignmentAdminReportResponse() {
    }

    public AssignmentAdminReportResponse(Long id, String title, String trainerEmail, Long batchId,
                                          LocalDateTime deadline, Integer maxMarks, LocalDateTime createdAt,
                                          long submissionCount, String organizationId) {
        this.id = id;
        this.title = title;
        this.trainerEmail = trainerEmail;
        this.batchId = batchId;
        this.deadline = deadline;
        this.maxMarks = maxMarks;
        this.createdAt = createdAt;
        this.submissionCount = submissionCount;
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

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }

    public Integer getMaxMarks() { return maxMarks; }
    public void setMaxMarks(Integer maxMarks) { this.maxMarks = maxMarks; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public long getSubmissionCount() { return submissionCount; }
    public void setSubmissionCount(long submissionCount) { this.submissionCount = submissionCount; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
}