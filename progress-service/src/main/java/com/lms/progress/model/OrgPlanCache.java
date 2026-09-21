package com.lms.progress.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "org_plan_cache")
public class OrgPlanCache {

    @Id
    @Column(name = "organization_id")
    private String organizationId;

    private String plan;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public String getOrganizationId()            { return organizationId; }
    public void setOrganizationId(String orgId)  { this.organizationId = orgId; }

    public String getPlan()           { return plan; }
    public void setPlan(String plan)  { this.plan = plan; }

    public LocalDateTime getUpdatedAt()             { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}