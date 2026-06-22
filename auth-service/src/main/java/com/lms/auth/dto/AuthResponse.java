

package com.lms.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID; // ← ADDED

public class AuthResponse {

    private String token;
    private String email;
    private String role;
    private String name;

    @JsonProperty("isNewUser")
    private boolean isNewUser;
    private boolean profileCompleted;
    private boolean newUser;

    private UUID organizationId; // ← ADDED

    public AuthResponse(String token, String email, String role, String name) {
        this.token = token;
        this.email = email;
        this.role = role;
        this.name = name;
        this.isNewUser = false;
        this.newUser = false;
        this.profileCompleted = true;
        this.organizationId = null; // ← ADDED
    }

    public AuthResponse(String token, String email, String role) {
        this.token = token;
        this.email = email;
        this.role = role;
        this.isNewUser = false;
        this.newUser = false;
        this.profileCompleted = true;
        this.organizationId = null; // ← ADDED
    }

    public String getToken() { return token; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getName() { return name; }
    public boolean isNewUser() { return isNewUser; }
    public boolean isProfileCompleted() { return profileCompleted; }
    public boolean getNewUser() { return newUser; }
    public UUID getOrganizationId() { return organizationId; } // ← ADDED

    public void setToken(String token) { this.token = token; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setName(String name) { this.name = name; }
    public void setNewUser(boolean newUser) {
        this.isNewUser = newUser;
        this.newUser = newUser;
    }
    public void setProfileCompleted(boolean profileCompleted) {
        this.profileCompleted = profileCompleted;
    }
    public void setOrganizationId(UUID organizationId) { // ← ADDED
        this.organizationId = organizationId;
    }
}
