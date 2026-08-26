package com.lms.progress.dto;

import java.util.List;

public class BatchUpdateProgressResponse {

    private String orgId;
    private Long orgRoadmapId;
    private List<NodeProgressResponse> updated;
    private Integer successCount;
    private Integer failureCount;

    public BatchUpdateProgressResponse() {
    }

    public BatchUpdateProgressResponse(String orgId, Long orgRoadmapId, List<NodeProgressResponse> updated,
                                        Integer successCount, Integer failureCount) {
        this.orgId = orgId;
        this.orgRoadmapId = orgRoadmapId;
        this.updated = updated;
        this.successCount = successCount;
        this.failureCount = failureCount;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public Long getOrgRoadmapId() {
        return orgRoadmapId;
    }

    public void setOrgRoadmapId(Long orgRoadmapId) {
        this.orgRoadmapId = orgRoadmapId;
    }

    public List<NodeProgressResponse> getUpdated() {
        return updated;
    }

    public void setUpdated(List<NodeProgressResponse> updated) {
        this.updated = updated;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Integer getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(Integer failureCount) {
        this.failureCount = failureCount;
    }
}