package com.lms.progress.dto;

import java.util.List;

public class RoadmapAnalyticsResponse {

    private Long orgRoadmapId;
    private String orgId;
    private Integer totalStudents;
    private Double completionPercent;
    private List<NodeBottleneckStat> nodeBottleneckStats;

    public RoadmapAnalyticsResponse() {
    }

    public RoadmapAnalyticsResponse(Long orgRoadmapId, String orgId, Integer totalStudents, Double completionPercent,
                                     List<NodeBottleneckStat> nodeBottleneckStats) {
        this.orgRoadmapId = orgRoadmapId;
        this.orgId = orgId;
        this.totalStudents = totalStudents;
        this.completionPercent = completionPercent;
        this.nodeBottleneckStats = nodeBottleneckStats;
    }

    public Long getOrgRoadmapId() {
        return orgRoadmapId;
    }

    public void setOrgRoadmapId(Long orgRoadmapId) {
        this.orgRoadmapId = orgRoadmapId;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public Integer getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(Integer totalStudents) {
        this.totalStudents = totalStudents;
    }

    public Double getCompletionPercent() {
        return completionPercent;
    }

    public void setCompletionPercent(Double completionPercent) {
        this.completionPercent = completionPercent;
    }

    public List<NodeBottleneckStat> getNodeBottleneckStats() {
        return nodeBottleneckStats;
    }

    public void setNodeBottleneckStats(List<NodeBottleneckStat> nodeBottleneckStats) {
        this.nodeBottleneckStats = nodeBottleneckStats;
    }
}