package com.lms.live_session.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Mirror of auth-service AuthEvent — consumed by live-session-service from topic: auth-events
 * Must stay in sync with com.lms.auth.event.AuthEvent
 *
 * NOTE: organizationId is a String here (UUID text, matching the wire format),
 * NOT a Long — even though this service's own entities use Long organizationId.
 * Convert explicitly at the resolver boundary (see LiveSessionTierResolver).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthEvent {

    private String eventType;
    private String email;
    private String role;
    private String organizationId;
    private String plan;

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }
}
