package com.lms.progress.dto;

import java.util.List;

public class StudentDashboardResponse {

    private Long userId;
    private String orgId;
    private List<EnrolledRoadmapSummary> enrolledRoadmaps;
    private Integer totalEnrolled;
    private Double averageCompletionPercent;

    public StudentDashboardResponse() {
    }

    public StudentDashboardResponse(Long userId, String orgId, List<EnrolledRoadmapSummary> enrolledRoadmaps,
                                     Integer totalEnrolled, Double averageCompletionPercent) {
        this.userId = userId;
        this.orgId = orgId;
        this.enrolledRoadmaps = enrolledRoadmaps;
        this.totalEnrolled = totalEnrolled;
        this.averageCompletionPercent = averageCompletionPercent;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public List<EnrolledRoadmapSummary> getEnrolledRoadmaps() {
        return enrolledRoadmaps;
    }

    public void setEnrolledRoadmaps(List<EnrolledRoadmapSummary> enrolledRoadmaps) {
        this.enrolledRoadmaps = enrolledRoadmaps;
    }

    public Integer getTotalEnrolled() {
        return totalEnrolled;
    }

    public void setTotalEnrolled(Integer totalEnrolled) {
        this.totalEnrolled = totalEnrolled;
    }

    public Double getAverageCompletionPercent() {
        return averageCompletionPercent;
    }

    public void setAverageCompletionPercent(Double averageCompletionPercent) {
        this.averageCompletionPercent = averageCompletionPercent;
    }
}