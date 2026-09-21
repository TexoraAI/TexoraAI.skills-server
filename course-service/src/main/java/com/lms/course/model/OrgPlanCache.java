package com.lms.course.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "org_plan_cache")
public class OrgPlanCache {

    @Id
    @Column(name = "organization_id")
    private String organizationId;

    private String plan;

    private LocalDateTime updatedAt;

    public OrgPlanCache() {}

    public String getOrganizationId()                 { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public String getPlan()                           { return plan; }
    public void setPlan(String plan)                  { this.plan = plan; }

    public LocalDateTime getUpdatedAt()                { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt)  { this.updatedAt = updatedAt; }
}