package com.lms.live_session.dto;

public class TexoraJoinRequestDTO {
    private String identity;
    private String displayName;
    private String role;

    public TexoraJoinRequestDTO() {}

    public String getIdentity() { return identity; }
    public void setIdentity(String identity) { this.identity = identity; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}