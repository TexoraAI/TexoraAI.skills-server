package com.lms.progress.dto;

import java.util.List;

public class BatchUpdateProgressRequest {

    private Long orgRoadmapId;
    private List<NodeStatusPair> updates;

    public BatchUpdateProgressRequest() {
    }

    public BatchUpdateProgressRequest(Long orgRoadmapId, List<NodeStatusPair> updates) {
        this.orgRoadmapId = orgRoadmapId;
        this.updates = updates;
    }

    public Long getOrgRoadmapId() {
        return orgRoadmapId;
    }

    public void setOrgRoadmapId(Long orgRoadmapId) {
        this.orgRoadmapId = orgRoadmapId;
    }

    public List<NodeStatusPair> getUpdates() {
        return updates;
    }

    public void setUpdates(List<NodeStatusPair> updates) {
        this.updates = updates;
    }
}
