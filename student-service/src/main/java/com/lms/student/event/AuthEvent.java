package com.lms.student.event;

public class AuthEvent {

    private String eventType;   // USER_CREATED, USER_DELETED, USER_ROLE_CHANGED
    private Long userId;        // 🔑 KEY (auth.users.id)
    private String email;
    private String role;
    private String displayName;

    public String getEventType() { return eventType; }
    public Long getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getDisplayName() { return displayName; }

    public void setEventType(String eventType) { this.eventType = eventType; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
}
