package com.lms.course.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FeaturedProgramKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${topics.featured-content}")
    private String featuredContentTopic;

    @Value("${topics.featured-program-lifecycle}")
    private String featuredProgramLifecycleTopic;

    public FeaturedProgramKafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishFeaturedVideoDeleted(Long sessionId, String oldUrl) {
        Map<String, Object> event = Map.of(
                "type", "FEATURED_VIDEO_DELETED",
                "payload", Map.of(
                        "sessionId", sessionId,
                        "oldUrl", oldUrl,
                        "contentType", "VIDEO"
                )
        );
        kafkaTemplate.send(featuredContentTopic, event);
        kafkaTemplate.flush();
        System.out.println("📤 Sent FEATURED_VIDEO_DELETED → sessionId=" + sessionId + " oldUrl=" + oldUrl);
    }

    public void publishFeaturedVideoUpdated(Long sessionId, String oldUrl, String newUrl) {
        Map<String, Object> event = Map.of(
                "type", "FEATURED_VIDEO_UPDATED",
                "payload", Map.of(
                        "sessionId", sessionId,
                        "oldUrl", oldUrl,
                        "newUrl", newUrl,
                        "contentType", "VIDEO"
                )
        );
        kafkaTemplate.send(featuredContentTopic, event);
        kafkaTemplate.flush();
        System.out.println("📤 Sent FEATURED_VIDEO_UPDATED → sessionId=" + sessionId
                + " oldUrl=" + oldUrl + " newUrl=" + newUrl);
    }

    public void publishFeaturedProgramDeleted(Long programId, List<Long> sessionIds) {
        Map<String, Object> event = Map.of(
                "type", "FEATURED_PROGRAM_DELETED",
                "payload", Map.of(
                        "programId", programId,
                        "sessionIds", sessionIds
                )
        );
        kafkaTemplate.send(featuredProgramLifecycleTopic, event);
        kafkaTemplate.flush();
        System.out.println("📤 Sent FEATURED_PROGRAM_DELETED → programId=" + programId
                + " sessionIds=" + sessionIds);
    }

    // ── NEW: file equivalents of the video methods above ──────────────────────
    // used by FeaturedSyllabusFileService.deleteFile()
    public void publishFeaturedFileDeleted(Long sessionId, String oldUrl) {
        Map<String, Object> event = Map.of(
                "type", "FEATURED_FILE_DELETED",
                "payload", Map.of(
                        "sessionId", sessionId,
                        "oldUrl", oldUrl,
                        "contentType", "FILE"
                )
        );
        kafkaTemplate.send(featuredContentTopic, event);
        kafkaTemplate.flush();
        System.out.println("📤 Sent FEATURED_FILE_DELETED → sessionId=" + sessionId + " oldUrl=" + oldUrl);
    }

    // used when a session's file is replaced (future use, mirrors video)
    public void publishFeaturedFileUpdated(Long sessionId, String oldUrl, String newUrl) {
        Map<String, Object> event = Map.of(
                "type", "FEATURED_FILE_UPDATED",
                "payload", Map.of(
                        "sessionId", sessionId,
                        "oldUrl", oldUrl,
                        "newUrl", newUrl,
                        "contentType", "FILE"
                )
        );
        kafkaTemplate.send(featuredContentTopic, event);
        kafkaTemplate.flush();
        System.out.println("📤 Sent FEATURED_FILE_UPDATED → sessionId=" + sessionId
                + " oldUrl=" + oldUrl + " newUrl=" + newUrl);
    }
}