package com.lms.payment.security;

import java.util.UUID;

public class AuthenticatedUser {

    private final String email;
    private final Long userId;
    private final UUID organizationId;
    private final String role;

    public AuthenticatedUser(String email, Long userId, UUID organizationId, String role) {
        this.email = email;
        this.userId = userId;
        this.organizationId = organizationId;
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public Long getUserId() {
        return userId;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public String getRole() {
        return role;
    }
}