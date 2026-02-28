//
//package com.lms.auth.event;
//
//public class AuthEvent {
//
//    private String eventType;   // USER_CREATED, USER_DELETED
//    private Long userId;
//    private String email;
//    private String role;        // STUDENT / TRAINER / BUSINESS / ADMIN
//    private String displayName; // optional
//
//    // ✅ REQUIRED CONSTRUCTOR (THIS FIXES YOUR ERROR)
//    public AuthEvent(String eventType, String email, String displayName, String role) {
//        this.eventType = eventType;
//        this.email = email;
//        this.displayName = displayName;
//        this.role = role;
//        
//    }
//
//    // ✅ NO-ARGS constructor (needed by Jackson)
//    public AuthEvent() {
//    }
//
//    // Getters
//    public String getEventType() { return eventType; }
//    public Long getUserId() { return userId; }
//    public String getEmail() { return email; }
//    public String getRole() { return role; }
//    public String getDisplayName() { return displayName; }
//    
//
//    // Setters
//    public void setEventType(String eventType) { this.eventType = eventType; }
//    public void setUserId(Long userId) { this.userId = userId; }
//    public void setEmail(String email) { this.email = email; }
//    public void setRole(String role) { this.role = role; }
//    public void setDisplayName(String displayName) { this.displayName = displayName; }
//}




package com.lms.auth.event;

public class AuthEvent {

    private String eventType;
    private Long userId;
    private String email;
    private String role;
    private String displayName;

    // ✅ REQUIRED by Jackson
    public AuthEvent() {
    }

    // ✅ REQUIRED by your service code
    public AuthEvent(
            String eventType,
            Long userId,
            String email,
            String role,
            String displayName
    ) {
        this.eventType = eventType;
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.displayName = displayName;
    }

    // -------- getters & setters --------

    public String getEventType() {
        return eventType;
    }

    public Long getUserId() {
        return userId;
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

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
