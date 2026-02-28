package com.lms.auth.event;

public class UserEvent {

	 private Long userId; 
    private String eventType;   // USER_UPDATED, USER_ROLE_CHANGED, USER_DELETED
    private String email;
    private String role;
    private String displayName;

    public UserEvent() {}

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getEventType() {
        return eventType;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getDisplayName() {
        return displayName;
    }
}
