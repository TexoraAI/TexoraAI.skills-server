package com.lms.file.event;

/**
 * Mirror of auth-service AuthEvent — consumed by file-service from topic:
 * auth-events via the existing kafkaListenerContainerFactory
 * (JsonMessageConverter). Must stay in sync with com.lms.auth.event.AuthEvent.
 */
public class AuthEvent {

    private String eventType;
    private String email;
    private String role;
    private String organizationId;
    private String plan;

    public AuthEvent() {
    }

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
