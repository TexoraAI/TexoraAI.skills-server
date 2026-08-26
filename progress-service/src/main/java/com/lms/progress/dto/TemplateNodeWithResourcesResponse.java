package com.lms.progress.dto;

import java.util.List;

public class TemplateNodeWithResourcesResponse extends TemplateNodeResponse {

    private List<TemplateResourceResponse> resources;

    public List<TemplateResourceResponse> getResources() {
        return resources;
    }

    public void setResources(List<TemplateResourceResponse> resources) {
        this.resources = resources;
    }
}