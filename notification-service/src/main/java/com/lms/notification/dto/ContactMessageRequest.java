package com.lms.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ContactMessageRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    private String email;

    private String phoneNumber;   // optional

    private String topic;         // optional

    @NotBlank(message = "Message cannot be empty")
    private String message;

    // ── Constructors ──────────────────────────────────────────
    public ContactMessageRequest() {}

    public ContactMessageRequest(String fullName, String email,
                                  String phoneNumber, String topic, String message) {
        this.fullName    = fullName;
        this.email       = email;
        this.phoneNumber = phoneNumber;
        this.topic       = topic;
        this.message     = message;
    }

    // ── Getters & Setters ─────────────────────────────────────
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    @Override
    public String toString() {
        return "ContactMessageRequest{fullName='" + fullName +
               "', email='" + email + "', topic='" + topic + "'}";
    }
}