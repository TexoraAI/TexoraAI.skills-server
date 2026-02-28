package com.lms.auth.dto;

public class UserApprovalStatusResponse {

    private String email;
    private String role;
    private String status; // PENDING | APPROVED | REJECTED

    public UserApprovalStatusResponse() {}

    public UserApprovalStatusResponse(String email, String role, String status) {
        this.email = email;
        this.role = role;
        this.status = status;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
