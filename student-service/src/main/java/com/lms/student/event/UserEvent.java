//package com.lms.student.event;
//
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//
//@JsonIgnoreProperties(ignoreUnknown = true)
//public class UserEvent {
//
//    private String eventType;
//    private Long userId;
//    private String email;
//    private String displayName;
//    private String oldRole;
//    private String newRole;
//
//    public UserEvent() {}
//
//    // Getters
//    public String getEventType() {
//        return eventType;
//    }
//
//    public Long getUserId() {
//        return userId;
//    }
//
//    public String getEmail() {
//        return email;
//    }
//
//    public String getDisplayName() {
//        return displayName;
//    }
//
//    public String getOldRole() {
//        return oldRole;
//    }
//
//    public String getNewRole() {
//        return newRole;
//    }
//
//    // Setters
//    public void setEventType(String eventType) {
//        this.eventType = eventType;
//    }
//
//    public void setUserId(Long userId) {
//        this.userId = userId;
//    }
//
//    public void setEmail(String email) {
//        this.email = email;
//    }
//
//    public void setDisplayName(String displayName) {
//        this.displayName = displayName;
//    }
//
//    public void setOldRole(String oldRole) {
//        this.oldRole = oldRole;
//    }
//
//    public void setNewRole(String newRole) {
//        this.newRole = newRole;
//    }
//}



package com.lms.student.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserEvent {

    private String eventType;
    private Long userId;
    private String email;
    private String displayName;
    private String role;   // <-- THIS IS THE KEY

    public UserEvent() {}

    public String getEventType() { return eventType; }
    public Long getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getDisplayName() { return displayName; }
    public String getRole() { return role; }

    public void setEventType(String eventType) { this.eventType = eventType; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setEmail(String email) { this.email = email; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setRole(String role) { this.role = role; }
}
