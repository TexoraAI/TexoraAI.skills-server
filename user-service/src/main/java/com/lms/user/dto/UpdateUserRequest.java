package com.lms.user.dto;

public class UpdateUserRequest {
    private String displayName;
    private String roles;
    
    // ✅ ADDED: accept Base64 photo string from frontend
    private String photoUrl;

    public UpdateUserRequest() {}

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getRoles() { return roles; }
    public void setRoles(String roles) { this.roles = roles; }
    
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
}
