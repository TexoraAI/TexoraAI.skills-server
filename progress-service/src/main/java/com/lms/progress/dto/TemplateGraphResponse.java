package com.lms.progress.dto;

import java.util.List;

public class TemplateGraphResponse {

    private Long templateId;
    private String title;
    private String slug;
    private List<TemplateNodeWithResourcesResponse> nodes;
    private List<GraphEdgeResponse> edges;

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
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

    public List<TemplateNodeWithResourcesResponse> getNodes() {
        return nodes;
    }

    public void setNodes(List<TemplateNodeWithResourcesResponse> nodes) {
        this.nodes = nodes;
    }

    public List<GraphEdgeResponse> getEdges() {
        return edges;
    }

    public void setEdges(List<GraphEdgeResponse> edges) {
        this.edges = edges;
    }
}