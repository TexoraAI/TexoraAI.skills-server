package com.lms.assessment.dto;

import java.time.LocalDateTime;

public class SubmissionResponse {

    private Long id;
    private Long assignmentId;

    private String studentEmail;
    private String fileName;
    private String status;
    private Integer obtainedMarks;
    private LocalDateTime submittedAt;
    private String downloadUrl;

    public SubmissionResponse(
            Long id,
            Long assignmentId,
            String studentEmail,
            String fileName,
            String status,
            Integer obtainedMarks,
            LocalDateTime submittedAt,
            String downloadUrl) {

        this.id = id;
        this.assignmentId=assignmentId;
        this.studentEmail = studentEmail;
        this.fileName = fileName;
        this.status = status;
        this.obtainedMarks = obtainedMarks;
        this.submittedAt = submittedAt;
        this.downloadUrl = downloadUrl;
    }


    // Getters
    public Long getId() { return id; }
    public Long getAssignmentId() { return assignmentId; }

    public String getStudentEmail() {return studentEmail;}
    public String getFileName() { return fileName; }
    public String getStatus() { return status; }
    public Integer getObtainedMarks() { return obtainedMarks; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public String getDownloadUrl() { return downloadUrl; }
}
