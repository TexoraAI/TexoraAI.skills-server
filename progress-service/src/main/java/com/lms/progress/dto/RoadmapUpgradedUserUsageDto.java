package com.lms.progress.dto;

/**
 * "Who used most roadmaps" unit, reused across RoadmapUpgradedAdminStatsDto
 * and RoadmapUpgradedSuperAdminStatsDto.
 */
public class RoadmapUpgradedUserUsageDto {

    private Long userId;
    private String userName;
    private String role;
    private String organizationId;
    private Long roadmapsGenerated;
    private Double avgCompletionPercent;

    public RoadmapUpgradedUserUsageDto() {
    }

    public RoadmapUpgradedUserUsageDto(Long userId,
                                        String userName,
                                        String role,
                                        String organizationId,
                                        Long roadmapsGenerated,
                                        Double avgCompletionPercent) {
        this.userId = userId;
        this.userName = userName;
        this.role = role;
        this.organizationId = organizationId;
        this.roadmapsGenerated = roadmapsGenerated;
        this.avgCompletionPercent = avgCompletionPercent;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public Long getRoadmapsGenerated() {
        return roadmapsGenerated;
    }

    public void setRoadmapsGenerated(Long roadmapsGenerated) {
        this.roadmapsGenerated = roadmapsGenerated;
    }

    public Double getAvgCompletionPercent() {
        return avgCompletionPercent;
    }

    public void setAvgCompletionPercent(Double avgCompletionPercent) {
        this.avgCompletionPercent = avgCompletionPercent;
    }
}
