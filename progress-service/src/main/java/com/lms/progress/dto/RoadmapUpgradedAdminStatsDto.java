package com.lms.progress.dto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * For ADMIN/TENANT_ADMIN dashboards - org-scoped only.
 */
public class RoadmapUpgradedAdminStatsDto {

    private String organizationId;
    private Long totalRoadmapsInOrg;
    private Long totalStudentsInOrg;
    private Long totalTrainersInOrg;
    private Map<String, Long> pathTypeBreakdown = new LinkedHashMap<>();
    private List<RoadmapUpgradedUserUsageDto> topStudentsByUsage = new ArrayList<>();
    private List<RoadmapUpgradedUserUsageDto> topTrainersByUsage = new ArrayList<>();

    public RoadmapUpgradedAdminStatsDto() {
    }

    public RoadmapUpgradedAdminStatsDto(String organizationId,
                                         Long totalRoadmapsInOrg,
                                         Long totalStudentsInOrg,
                                         Long totalTrainersInOrg,
                                         Map<String, Long> pathTypeBreakdown,
                                         List<RoadmapUpgradedUserUsageDto> topStudentsByUsage,
                                         List<RoadmapUpgradedUserUsageDto> topTrainersByUsage) {
        this.organizationId = organizationId;
        this.totalRoadmapsInOrg = totalRoadmapsInOrg;
        this.totalStudentsInOrg = totalStudentsInOrg;
        this.totalTrainersInOrg = totalTrainersInOrg;
        this.pathTypeBreakdown = pathTypeBreakdown != null ? pathTypeBreakdown : new LinkedHashMap<>();
        this.topStudentsByUsage = topStudentsByUsage != null ? topStudentsByUsage : new ArrayList<>();
        this.topTrainersByUsage = topTrainersByUsage != null ? topTrainersByUsage : new ArrayList<>();
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public Long getTotalRoadmapsInOrg() {
        return totalRoadmapsInOrg;
    }

    public void setTotalRoadmapsInOrg(Long totalRoadmapsInOrg) {
        this.totalRoadmapsInOrg = totalRoadmapsInOrg;
    }

    public Long getTotalStudentsInOrg() {
        return totalStudentsInOrg;
    }

    public void setTotalStudentsInOrg(Long totalStudentsInOrg) {
        this.totalStudentsInOrg = totalStudentsInOrg;
    }

    public Long getTotalTrainersInOrg() {
        return totalTrainersInOrg;
    }

    public void setTotalTrainersInOrg(Long totalTrainersInOrg) {
        this.totalTrainersInOrg = totalTrainersInOrg;
    }

    public Map<String, Long> getPathTypeBreakdown() {
        return pathTypeBreakdown;
    }

    public void setPathTypeBreakdown(Map<String, Long> pathTypeBreakdown) {
        this.pathTypeBreakdown = pathTypeBreakdown;
    }

    public List<RoadmapUpgradedUserUsageDto> getTopStudentsByUsage() {
        return topStudentsByUsage;
    }

    public void setTopStudentsByUsage(List<RoadmapUpgradedUserUsageDto> topStudentsByUsage) {
        this.topStudentsByUsage = topStudentsByUsage;
    }

    public List<RoadmapUpgradedUserUsageDto> getTopTrainersByUsage() {
        return topTrainersByUsage;
    }

    public void setTopTrainersByUsage(List<RoadmapUpgradedUserUsageDto> topTrainersByUsage) {
        this.topTrainersByUsage = topTrainersByUsage;
    }
}
