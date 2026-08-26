package com.lms.progress.dto;

import java.util.List;

public class OrgRoadmapGraphResponse {

    private Long orgRoadmapId;
    private String orgId;
    private String title;
    private String slug;
    private List<OrgNodeWithResourcesResponse> nodes;
    private List<GraphEdgeResponse> edges;

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

    public List<OrgNodeWithResourcesResponse> getNodes() {
        return nodes;
    }

    public void setNodes(List<OrgNodeWithResourcesResponse> nodes) {
        this.nodes = nodes;
    }

    public List<GraphEdgeResponse> getEdges() {
        return edges;
    }

    public void setEdges(List<GraphEdgeResponse> edges) {
        this.edges = edges;
    }
}