package com.lms.auth.dto;

public class ChangePasswordRequest {

    private String newPassword;
    private String confirmPassword;

    // getters
    public String getNewPassword() {
        return newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    // setters
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}