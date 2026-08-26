package com.lms.progress.dto;

import java.util.List;

public class RoadmapGraphResponse {

    private Long orgRoadmapId;
    private String orgId;
    private String title;
    private String slug;
    private String description;
    private List<GraphNodeResponse> nodes;
    private List<GraphEdgeResponse> edges;
    private Double overallCompletionPercent;

    public RoadmapGraphResponse() {
    }

    public RoadmapGraphResponse(Long orgRoadmapId, String orgId, String title, String slug, String description,
                                 List<GraphNodeResponse> nodes, List<GraphEdgeResponse> edges,
                                 Double overallCompletionPercent) {
        this.orgRoadmapId = orgRoadmapId;
        this.orgId = orgId;
        this.title = title;
        this.slug = slug;
        this.description = description;
        this.nodes = nodes;
        this.edges = edges;
        this.overallCompletionPercent = overallCompletionPercent;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<GraphNodeResponse> getNodes() {
        return nodes;
    }

    public void setNodes(List<GraphNodeResponse> nodes) {
        this.nodes = nodes;
    }

    public List<GraphEdgeResponse> getEdges() {
        return edges;
    }

    public void setEdges(List<GraphEdgeResponse> edges) {
        this.edges = edges;
    }

    public Double getOverallCompletionPercent() {
        return overallCompletionPercent;
    }

    public void setOverallCompletionPercent(Double overallCompletionPercent) {
        this.overallCompletionPercent = overallCompletionPercent;
    }
}