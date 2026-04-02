package com.lms.user.dto;

import java.time.Instant;

public class UserResponse {
    private Long id;
    private String email;
    private String displayName;
    private String roles;
    private String tenantId;
    private Instant createdAt;
    //updted
    private String photoUrl;

    public UserResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getRoles() { return roles; }
    public void setRoles(String roles) { this.roles = roles; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
 // ✅ ADDED: getter & setter for photoUrl
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
}
