package com.lms.auth.dto;

public class AdminUpdateUserRequest {
    private String name;
    private String email;
    private String role; // optional, e.g. "STUDENT", "TRAINER"

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}