package com.lms.auth.dto;

import com.lms.auth.model.Role;

public class RegisterRequest {

    // ✅ NEW FIELD
    private String name;

    private String email;
    private String password;
    private Role role; // STUDENT / TRAINER / ADMIN / BUSINESS
    private String organizationId;
    // Getter & Setter for name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Existing getters & setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
}
