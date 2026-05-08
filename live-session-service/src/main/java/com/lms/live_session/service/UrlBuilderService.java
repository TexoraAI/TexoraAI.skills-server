package com.lms.live_session.service;

import com.lms.live_session.config.BaseUrlConfig;
import org.springframework.stereotype.Service;

@Service
public class UrlBuilderService {

    private final BaseUrlConfig baseUrlConfig;

    public UrlBuilderService(BaseUrlConfig baseUrlConfig) {
        this.baseUrlConfig = baseUrlConfig;
    }

    /**
     * ✅ Generate public booking join link
     */
    public String generatePublicJoinLink(String accessToken) {
        return baseUrlConfig.getFrontendBaseUrl() + "/public/join-session/" + accessToken;
    }

    /**
     * ✅ Generate trainer live controls link
     */
    public String generateTrainerLiveLink(Long sessionId) {
        return baseUrlConfig.getFrontendBaseUrl() + "/trainer/live-controls/" + sessionId;
    }

    /**
     * ✅ Generate student live join link
     */
    public String generateStudentLiveLink(Long sessionId) {
        return baseUrlConfig.getFrontendBaseUrl() + "/student/live/" + sessionId;
    }

    /**
     * ✅ Generate API endpoint for session details
     */
    public String getSessionDetailsApi(Long sessionId) {
        return baseUrlConfig.getApiBaseUrl() + "/live-sessions/" + sessionId;
    }
}