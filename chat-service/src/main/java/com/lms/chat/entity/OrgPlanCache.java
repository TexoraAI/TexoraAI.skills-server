package com.lms.chat.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "org_plan_cache")
public class OrgPlanCache {

    @Id
    private String organizationId;

    private String plan;

    private LocalDateTime updatedAt;

    public String getOrganizationId()                    { return organizationId; }
    public void setOrganizationId(String organizationId)  { this.organizationId = organizationId; }

    public String getPlan()                     { return plan; }
    public void setPlan(String plan)            { this.plan = plan; }

    public LocalDateTime getUpdatedAt()                     { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt)       { this.updatedAt = updatedAt; }
}