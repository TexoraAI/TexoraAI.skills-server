
package com.lms.user.event;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
/**
 * Mirror of auth-service AuthEvent — consumed by user-service from topic: auth-events
 * Must stay in sync with com.lms.auth.event.AuthEvent
 */
@JsonIgnoreProperties(ignoreUnknown = true) 
public class AuthEvent {

    private String eventType;
    private Long   userId;
    private String email;
    private String role;
    private String displayName;
    private String organizationId; // ← NEW: must match auth-service AuthEvent
    private String plan;
    private String expiresAt;

    public String getEventType()      { return eventType; }
    public void setEventType(String e){ this.eventType = e; }

    public Long getUserId()           { return userId; }
    public void setUserId(Long id)    { this.userId = id; }

    public String getEmail()          { return email; }
    public void setEmail(String e)    { this.email = e; }

    public String getRole()           { return role; }
    public void setRole(String r)     { this.role = r; }

    public String getDisplayName()              { return displayName; }
    public void setDisplayName(String dn)       { this.displayName = dn; }

    public String getOrganizationId()           { return organizationId; }   // ← NEW
    public void setOrganizationId(String orgId) { this.organizationId = orgId; } // ← NEW
    

    public String getPlan()           { return plan; }
    public void setPlan(String plan)  { this.plan = plan; }
    
    public String getExpiresAt()           { return expiresAt; }
    public void setExpiresAt(String v)     { this.expiresAt = v; }
}