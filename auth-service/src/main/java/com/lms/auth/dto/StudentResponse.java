package com.lms.auth.dto;

public class StudentResponse {

    private Long userId;
    private String fullName;
    private String email;
    private boolean approved;

    public StudentResponse() {}

    public StudentResponse(Long userId, String fullName, String email, boolean approved) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.approved = approved;
    }

    public Long getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public boolean isApproved() {
        return approved;
    }
}
