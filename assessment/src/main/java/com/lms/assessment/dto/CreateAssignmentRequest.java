package com.lms.assessment.dto;

import java.time.LocalDateTime;

public class CreateAssignmentRequest {

    private String title;
    private String description;
    private Long batchId;
    private LocalDateTime deadline;
    private Integer maxMarks;
    private String duration;

    // Getters & Setters

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }

    public Integer getMaxMarks() { return maxMarks; }
    public void setMaxMarks(Integer maxMarks) { this.maxMarks = maxMarks; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
}
