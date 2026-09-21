package com.lms.course.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_plan_cache")
public class UserPlanCache {

    @Id
    private String email;

    private String plan;

    @Column(name = "organization_id")
    private String organizationId;

    private LocalDateTime updatedAt;

    public UserPlanCache() {}

    public String getEmail()                          { return email; }
    public void setEmail(String email)                { this.email = email; }

    public String getPlan()                           { return plan; }
    public void setPlan(String plan)                  { this.plan = plan; }

    public String getOrganizationId()                 { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public LocalDateTime getUpdatedAt()                { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt)  { this.updatedAt = updatedAt; }
}