package com.lms.file.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class FeaturedFileKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${topics.featured-file-status}")
    private String featuredFileStatusTopic;

    public FeaturedFileKafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishFileReady(Long sessionId, String fileId, String url, String fileName) {
        Map<String, Object> event = Map.of(
                "type", "FILE_READY",
                "payload", Map.of(
                        "sessionId", sessionId,
                        "fileId", fileId,
                        "url", url,
                        "fileName", fileName
                )
        );
        kafkaTemplate.send(featuredFileStatusTopic, event);
        kafkaTemplate.flush();
        System.out.println("📤 Sent FILE_READY → sessionId=" + sessionId + " url=" + url);
    }

    public void publishFileFailed(Long sessionId, String reason) {
        Map<String, Object> event = Map.of(
                "type", "FILE_FAILED",
                "payload", Map.of(
                        "sessionId", sessionId,
                        "reason", reason != null ? reason : "unknown error"
                )
        );
        kafkaTemplate.send(featuredFileStatusTopic, event);
        kafkaTemplate.flush();
        System.out.println("📤 Sent FILE_FAILED → sessionId=" + sessionId + " reason=" + reason);
    }
}