package com.lms.course.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Mirror of auth-service AuthEvent — consumed by course-service from topic: auth-events
 * Must stay in sync with com.lms.auth.event.AuthEvent
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthEvent {

    private String eventType;
    private String email;
    private String role;
    private String organizationId;
    private String plan;

    public String getEventType()               { return eventType; }
    public void setEventType(String eventType)  { this.eventType = eventType; }

    public String getEmail()                    { return email; }
    public void setEmail(String email)          { this.email = email; }

    public String getRole()                     { return role; }
    public void setRole(String role)            { this.role = role; }

    public String getOrganizationId()                    { return organizationId; }
    public void setOrganizationId(String organizationId)  { this.organizationId = organizationId; }

    public String getPlan()                     { return plan; }
    public void setPlan(String plan)            { this.plan = plan; }
}