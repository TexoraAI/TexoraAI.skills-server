package com.lms.progress.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Feature-scoped YouTube Data API v3 configuration for the RoadmapUpgraded
 * feature only - mirrors RoadmapUpgradedOpenAiConfig exactly (see that class
 * for why this stays self-contained instead of reusing a shared bean).
 *
 * Reads its own dedicated properties (see application.yml additions):
 *
 *   roadmap-upgraded:
 *     youtube:
 *       api-key: ${YOUTUBE_API_KEY}
 *       api-url: https://www.googleapis.com/youtube/v3/search
 *       connect-timeout-ms: 5000
 *       read-timeout-ms: 15000
 *
 * If you already have a shared YouTube/Google API client bean elsewhere in
 * the project, point RoadmapUpgradedYoutubeClient at it instead and delete
 * this file - the rest of the feature only depends on
 * RoadmapUpgradedYoutubeClient's public methods, not on this config directly.
 */
@Configuration
public class RoadmapUpgradedYoutubeConfig {

    @Value("${roadmap-upgraded.youtube.api-key}")
    private String apiKey;

    @Value("${roadmap-upgraded.youtube.api-url:https://www.googleapis.com/youtube/v3/search}")
    private String apiUrl;

    @Value("${roadmap-upgraded.youtube.connect-timeout-ms:5000}")
    private int connectTimeoutMs;

    @Value("${roadmap-upgraded.youtube.read-timeout-ms:15000}")
    private int readTimeoutMs;

    @Bean(name = "roadmapUpgradedYoutubeRestTemplate")
    public RestTemplate roadmapUpgradedYoutubeRestTemplate() {
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
}