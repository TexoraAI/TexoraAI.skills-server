package com.lms.auth.dto;

import com.lms.auth.model.Role;
import java.util.List;
import java.util.Map;

public class OnboardingSaveRequest {
    private Role role;
    private Map<String, List<String>> onboardingAnswers;

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Map<String, List<String>> getOnboardingAnswers() { return onboardingAnswers; }
    public void setOnboardingAnswers(Map<String, List<String>> onboardingAnswers) {
        this.onboardingAnswers = onboardingAnswers;
    }
}