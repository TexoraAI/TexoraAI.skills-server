package com.lms.live_session.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BaseUrlConfig {

    @Value("${app.base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    @Value("${app.api-base-url:http://localhost:9000/api}")
    private String apiBaseUrl;

    public String getFrontendBaseUrl() {
        return frontendBaseUrl;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }
}