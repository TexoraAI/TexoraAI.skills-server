package com.lms.progress.dto;

import java.time.LocalDateTime;

public class StudentProgressSummaryResponse {

    private Long studentId;
    private String studentName;
    private String orgId;
    private Long orgRoadmapId;
    private Double completionPercent;
    private LocalDateTime lastActiveAt;

    public StudentProgressSummaryResponse() {
    }

    public StudentProgressSummaryResponse(Long studentId, String studentName, String orgId, Long orgRoadmapId,
                                           Double completionPercent, LocalDateTime lastActiveAt) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.orgId = orgId;
        this.orgRoadmapId = orgRoadmapId;
        this.completionPercent = completionPercent;
        this.lastActiveAt = lastActiveAt;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public Long getOrgRoadmapId() {
        return orgRoadmapId;
    }

    public void setOrgRoadmapId(Long orgRoadmapId) {
        this.orgRoadmapId = orgRoadmapId;
    }

    public Double getCompletionPercent() {
        return completionPercent;
    }

    public void setCompletionPercent(Double completionPercent) {
        this.completionPercent = completionPercent;
    }

    public LocalDateTime getLastActiveAt() {
        return lastActiveAt;
    }

    public void setLastActiveAt(LocalDateTime lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }
}