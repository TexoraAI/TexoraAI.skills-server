package com.lms.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class NewsletterSubscribeRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    private String email;

    // ── Constructors ──────────────────────────────────────────
    public NewsletterSubscribeRequest() {}

    public NewsletterSubscribeRequest(String email) {
        this.email = email;
    }

    // ── Getters & Setters ─────────────────────────────────────
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return "NewsletterSubscribeRequest{email='" + email + "'}";
    }
}