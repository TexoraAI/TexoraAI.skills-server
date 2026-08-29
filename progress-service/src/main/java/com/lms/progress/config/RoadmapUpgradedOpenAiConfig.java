package com.lms.progress.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Feature-scoped OpenAI configuration for the RoadmapUpgraded feature only.
 *
 * This deliberately does NOT reuse any existing OpenAI bean/config from other
 * services in this codebase, because that existing bean/class was not shared
 * with me - I don't have its bean name, method signatures, or exact config
 * property keys, and guessing them would risk silently colliding with (or
 * duplicating) something that already exists.
 *
 * This bean reads its own dedicated properties (see application.yml
 * additions) so it is self-contained and safe to drop in:
 *
 *   roadmap-upgraded:
 *     openai:
 *       api-key: ${OPENAI_API_KEY}
 *       api-url: https://api.openai.com/v1/chat/completions
 *       model: gpt-4o-mini
 *       connect-timeout-ms: 5000
 *       read-timeout-ms: 30000
 *
 * If you already have a shared OpenAI client bean elsewhere in the project,
 * point RoadmapUpgradedOpenAiClient at it instead and delete this file -
 * the rest of the feature only depends on RoadmapUpgradedOpenAiClient's
 * public methods, not on this config directly.
 */
@Configuration
public class RoadmapUpgradedOpenAiConfig {

    @Value("${roadmap-upgraded.openai.api-key}")
    private String apiKey;

    @Value("${roadmap-upgraded.openai.api-url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${roadmap-upgraded.openai.model:gpt-4o-mini}")
    private String model;

    @Value("${roadmap-upgraded.openai.connect-timeout-ms:5000}")
    private int connectTimeoutMs;

    @Value("${roadmap-upgraded.openai.read-timeout-ms:30000}")
    private int readTimeoutMs;

    @Bean(name = "roadmapUpgradedRestTemplate")
    public RestTemplate roadmapUpgradedRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        return new RestTemplate(factory);
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getModel() {
        return model;
    }
}
