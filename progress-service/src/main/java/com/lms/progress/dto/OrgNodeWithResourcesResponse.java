package com.lms.progress.dto;

import java.util.List;

public class OrgNodeWithResourcesResponse extends OrgNodeResponse {

    private List<OrgResourceResponse> resources;

    public List<OrgResourceResponse> getResources() {
        return resources;
    }

    public void setResources(List<OrgResourceResponse> resources) {
        this.resources = resources;
    }
}