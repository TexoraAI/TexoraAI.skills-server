package com.lms.progress.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Mirror of auth-service AuthEvent — consumed by progress-service from topic: auth-events
 * Must stay in sync with com.lms.auth.event.AuthEvent
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthEvent {

    private String eventType;
    private String email;
    private String role;
    private String organizationId;
    private String plan;

    public String getEventType()      { return eventType; }
    public void setEventType(String e){ this.eventType = e; }

    public String getEmail()          { return email; }
    public void setEmail(String e)    { this.email = e; }

    public String getRole()           { return role; }
    public void setRole(String r)     { this.role = r; }

    public String getOrganizationId()           { return organizationId; }
    public void setOrganizationId(String orgId) { this.organizationId = orgId; }

    public String getPlan()           { return plan; }
    public void setPlan(String plan)  { this.plan = plan; }
}