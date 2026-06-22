

package com.lms.auth.dto;

import com.lms.auth.model.Role;
import java.util.List;
import java.util.Map;

public class GoogleLoginRequest {

    private String idToken;
    private Role role;

    // Onboarding popup answers from frontend
    // Key = "step_0", "step_1" ... Value = list of selected option labels
    private Map<String, List<String>> onboardingAnswers;

    public String getIdToken() { return idToken; }
    public void setIdToken(String idToken) { this.idToken = idToken; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Map<String, List<String>> getOnboardingAnswers() { return onboardingAnswers; }
    public void setOnboardingAnswers(Map<String, List<String>> onboardingAnswers) {
        this.onboardingAnswers = onboardingAnswers;
    }
}