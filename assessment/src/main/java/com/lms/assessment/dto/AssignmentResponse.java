package com.lms.assessment.dto;

import java.time.LocalDateTime;

public class AssignmentResponse {

    private Long id;
    private String title;
    private String description;
    private Long batchId;
    private LocalDateTime deadline;
    private Integer maxMarks;
    private String duration;
    private LocalDateTime createdAt;

    public AssignmentResponse(Long id, String title, String description,
                              Long batchId, LocalDateTime deadline,
                              Integer maxMarks, String duration,
                              LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.batchId = batchId;
        this.deadline = deadline;
        this.maxMarks = maxMarks;
        this.duration = duration;
        this.createdAt = createdAt;
    }

    // Getters

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Long getBatchId() { return batchId; }
    public LocalDateTime getDeadline() { return deadline; }
    public Integer getMaxMarks() { return maxMarks; }
    public String getDuration() { return duration; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
