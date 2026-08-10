package com.lms.video.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class FeaturedVideoKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${topics.featured-video-status}")
    private String featuredVideoStatusTopic;

    public FeaturedVideoKafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // ── used by FeaturedSessionVideoService.upload() on success ───────────────
  
    	public void publishVideoReady(Long sessionId, String videoId, String url,
                String thumbnailUrl, Integer durationSeconds,
                String title, String description) {
Map<String, Object> payload = new HashMap<>();
payload.put("sessionId", sessionId);
payload.put("videoId", videoId);
payload.put("url", url);
payload.put("thumbnailUrl", thumbnailUrl);
payload.put("durationSeconds", durationSeconds);
payload.put("title", title);
payload.put("description", description);

        Map<String, Object> event = Map.of(
                "type", "VIDEO_READY",
                "payload", payload
        );
        kafkaTemplate.send(featuredVideoStatusTopic, event);
        kafkaTemplate.flush();
        System.out.println("📤 Sent VIDEO_READY → sessionId=" + sessionId + " url=" + url);
    }

    // ── used by FeaturedSessionVideoService.upload() on IOException ───────────
    public void publishVideoFailed(Long sessionId, String reason) {
        Map<String, Object> event = Map.of(
                "type", "VIDEO_FAILED",
                "payload", Map.of(
                        "sessionId", sessionId,
                        "reason", reason != null ? reason : "unknown error"
                )
        );
        kafkaTemplate.send(featuredVideoStatusTopic, event);
        kafkaTemplate.flush();
        System.out.println("📤 Sent VIDEO_FAILED → sessionId=" + sessionId + " reason=" + reason);
    }
}