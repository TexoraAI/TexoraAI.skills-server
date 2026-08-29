package com.lms.progress.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * For SUPER_ADMIN dashboards - cross-org.
 */
public class RoadmapUpgradedSuperAdminStatsDto {

    private Long totalOrganizations;
    private Long totalRoadmapsPlatformWide;
    private List<RoadmapUpgradedAdminStatsDto> perOrgBreakdown = new ArrayList<>();
    private List<RoadmapUpgradedUserUsageDto> nullOrgStudents = new ArrayList<>();
    private List<RoadmapUpgradedUserUsageDto> nullOrgTrainers = new ArrayList<>();
    private List<RoadmapUpgradedUserUsageDto> topUsersPlatformWide = new ArrayList<>();

    public RoadmapUpgradedSuperAdminStatsDto() {
    }

    public RoadmapUpgradedSuperAdminStatsDto(Long totalOrganizations,
                                              Long totalRoadmapsPlatformWide,
                                              List<RoadmapUpgradedAdminStatsDto> perOrgBreakdown,
                                              List<RoadmapUpgradedUserUsageDto> nullOrgStudents,
                                              List<RoadmapUpgradedUserUsageDto> nullOrgTrainers,
                                              List<RoadmapUpgradedUserUsageDto> topUsersPlatformWide) {
        this.totalOrganizations = totalOrganizations;
        this.totalRoadmapsPlatformWide = totalRoadmapsPlatformWide;
        this.perOrgBreakdown = perOrgBreakdown != null ? perOrgBreakdown : new ArrayList<>();
        this.nullOrgStudents = nullOrgStudents != null ? nullOrgStudents : new ArrayList<>();
        this.nullOrgTrainers = nullOrgTrainers != null ? nullOrgTrainers : new ArrayList<>();
        this.topUsersPlatformWide = topUsersPlatformWide != null ? topUsersPlatformWide : new ArrayList<>();
    }

    public Long getTotalOrganizations() {
        return totalOrganizations;
    }

    public void setTotalOrganizations(Long totalOrganizations) {
        this.totalOrganizations = totalOrganizations;
    }

    public Long getTotalRoadmapsPlatformWide() {
        return totalRoadmapsPlatformWide;
    }

    public void setTotalRoadmapsPlatformWide(Long totalRoadmapsPlatformWide) {
        this.totalRoadmapsPlatformWide = totalRoadmapsPlatformWide;
    }

    public List<RoadmapUpgradedAdminStatsDto> getPerOrgBreakdown() {
        return perOrgBreakdown;
    }

    public void setPerOrgBreakdown(List<RoadmapUpgradedAdminStatsDto> perOrgBreakdown) {
        this.perOrgBreakdown = perOrgBreakdown;
    }

    public List<RoadmapUpgradedUserUsageDto> getNullOrgStudents() {
        return nullOrgStudents;
    }

    public void setNullOrgStudents(List<RoadmapUpgradedUserUsageDto> nullOrgStudents) {
        this.nullOrgStudents = nullOrgStudents;
    }

    public List<RoadmapUpgradedUserUsageDto> getNullOrgTrainers() {
        return nullOrgTrainers;
    }

    public void setNullOrgTrainers(List<RoadmapUpgradedUserUsageDto> nullOrgTrainers) {
        this.nullOrgTrainers = nullOrgTrainers;
    }

    public List<RoadmapUpgradedUserUsageDto> getTopUsersPlatformWide() {
        return topUsersPlatformWide;
    }

    public void setTopUsersPlatformWide(List<RoadmapUpgradedUserUsageDto> topUsersPlatformWide) {
        this.topUsersPlatformWide = topUsersPlatformWide;
    }
}
