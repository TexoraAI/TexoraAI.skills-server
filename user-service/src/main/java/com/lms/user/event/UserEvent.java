package com.lms.user.event;

public class UserEvent {

	 private Long userId; 
    private String eventType;   // USER_UPDATED, USER_ROLE_CHANGED, USER_DELETED
    private String email;
    private String role;
    private String displayName;

    public UserEvent() {}

    public UserEvent(String eventType, String email, String displayName, String role) {
        this.eventType = eventType;
        this.email = email;
        this.displayName = displayName;
        this.role = role;
    }

    public Long getUserId() { return userId; }
    public String getEventType() { return eventType; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getDisplayName() { return displayName; }

    
    public void setUserId(Long userId) { this.userId = userId; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
}
