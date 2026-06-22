package com.lms.auth.dto;

import java.util.List;
import java.util.Map;

public class OnboardingResponseDTO {

    private Long id;
    private String name;
    private String email;
    private String role;
    private String onboardingStatus;
    private String createdAt;
    private boolean googleUser;
    private Map<String, List<String>> onboardingAnswers;
    private boolean blocked;
    public OnboardingResponseDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getOnboardingStatus() { return onboardingStatus; }
    public void setOnboardingStatus(String onboardingStatus) { this.onboardingStatus = onboardingStatus; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public boolean isGoogleUser() { return googleUser; }
    public void setGoogleUser(boolean googleUser) { this.googleUser = googleUser; }

    public Map<String, List<String>> getOnboardingAnswers() { return onboardingAnswers; }
    public void setOnboardingAnswers(Map<String, List<String>> onboardingAnswers) {
        this.onboardingAnswers = onboardingAnswers;
    }
    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
}