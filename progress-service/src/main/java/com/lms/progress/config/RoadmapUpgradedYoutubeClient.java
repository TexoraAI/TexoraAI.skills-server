package com.lms.progress.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Thin wrapper around the YouTube Data API v3 "search" endpoint for the
 * RoadmapUpgraded feature. Mirrors RoadmapUpgradedOpenAiClient's shape: this
 * class only knows how to make the HTTP call and hand back the first
 * matching video ID - query construction happens in RoadmapUpgradedService.
 *
 * Not shared with any other feature's YouTube/Google API usage - see the
 * note in RoadmapUpgradedYoutubeConfig.
 */
@Component
public class RoadmapUpgradedYoutubeClient {

    private final RestTemplate restTemplate;
    private final RoadmapUpgradedYoutubeConfig config;
    private final ObjectMapper objectMapper;

    public RoadmapUpgradedYoutubeClient(RestTemplate roadmapUpgradedYoutubeRestTemplate,
                                         RoadmapUpgradedYoutubeConfig config,
                                         ObjectMapper objectMapper) {
        this.restTemplate = roadmapUpgradedYoutubeRestTemplate;
        this.config = config;
        this.objectMapper = objectMapper;
    }

    /**
     * Searches YouTube for the given query and returns the first result's
     * video ID, or null if the search returned no video results.
     *
     * Throws RoadmapUpgradedYoutubeException on any HTTP or parsing failure
     * (bad/missing key, quota exceeded, network error, unexpected response
     * shape) - callers are expected to catch this and treat it as "no video
     * available", same defensive style as the existing OpenAI-failure
     * handling elsewhere in this feature.
     */
    public String searchFirstVideoId(String query) {
        String url = UriComponentsBuilder.fromHttpUrl(config.getApiUrl())
                .queryParam("part", "snippet")
                .queryParam("type", "video")
                .queryParam("maxResults", 1)
                .queryParam("q", query)
                .queryParam("key", config.getApiKey())
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                String.class
        );

        if (response.getBody() == null) {
            throw new RoadmapUpgradedYoutubeException("Empty response body from YouTube");
        }

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode items = root.path("items");
            if (!items.isArray() || items.isEmpty()) {
                return null;
            }
            JsonNode videoId = items.get(0).path("id").path("videoId");
            return videoId.isMissingNode() ? null : videoId.asText(null);
        } catch (Exception e) {
            throw new RoadmapUpgradedYoutubeException("Failed to parse YouTube response: " + e.getMessage(), e);
        }
    }

    public static class RoadmapUpgradedYoutubeException extends RuntimeException {
        public RoadmapUpgradedYoutubeException(String message) {
            super(message);
        }

        public RoadmapUpgradedYoutubeException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}