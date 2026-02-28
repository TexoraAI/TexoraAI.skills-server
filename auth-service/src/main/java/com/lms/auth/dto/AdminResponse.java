package com.lms.auth.dto;

public class AdminResponse {

    private Long userId;
    private String fullName;
    private String email;
    private boolean approved;

    public AdminResponse() {
    }

    public AdminResponse(Long userId, String fullName, String email, boolean approved) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.approved = approved;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }
}
