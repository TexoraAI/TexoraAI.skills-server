package com.lms.auth.dto;

public class VerifyEmailRequest {

    private String email;
    private String token;

    public VerifyEmailRequest() {}

    public VerifyEmailRequest(String email, String token) {
        this.email = email;
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public String getToken() {
        return token;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
