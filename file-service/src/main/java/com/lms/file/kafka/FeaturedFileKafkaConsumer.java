package com.lms.file.kafka;

import com.lms.file.service.FeaturedSessionFileService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FeaturedFileKafkaConsumer {

    private final FeaturedSessionFileService service;

    public FeaturedFileKafkaConsumer(FeaturedSessionFileService service) {
        this.service = service;
    }

    // ── listens on the SAME topic as video's content events, filters by
    // contentType=="FILE" and ignores VIDEO — mirrors ContentEventConsumer's
    // PDF-only filter, just for the featured-content flow instead ──
    @KafkaListener(
            topics = "${topics.featured-content}",
            groupId = "file-service-group"
    )
    public void consumeContentEvents(Map<String, Object> message) {
        System.out.println("🔔 FILE FeaturedFileKafkaConsumer (content) received: " + message);
        try {
            String type = (String) message.get("type");
            if (type == null) return;

            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) message.get("payload");
            if (payload == null) return;

            String contentType = (String) payload.get("contentType");
            if (!"FILE".equals(contentType)) return; // ignore VIDEO events

            switch (type) {

                case "FEATURED_FILE_DELETED" -> {
                    String url = (String) payload.get("oldUrl");
                    if (url == null || url.isBlank()) {
                        System.out.println("⚠️ FEATURED_FILE_DELETED: oldUrl is null");
                        return;
                    }
                    service.deleteByUrl(url);
                }

                case "FEATURED_FILE_UPDATED" -> {
                    String oldUrl = (String) payload.get("oldUrl");
                    String newUrl = (String) payload.get("newUrl");
                    if (oldUrl == null || oldUrl.isBlank() || oldUrl.equals(newUrl)) {
                        return;
                    }
                    service.deleteByUrl(oldUrl);
                }

                default -> System.out.println("ℹ️ FILE ignoring featured-content event type=" + type);
            }

        } catch (Exception e) {
            System.out.println("❌ FeaturedFileKafkaConsumer (content) error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── listens for program deletion, bulk-cleans by sessionIds — same
    // pattern as video-service's program-lifecycle listener ──
    @KafkaListener(
            topics = "${topics.featured-program-lifecycle}",
            groupId = "file-service-group"
    )
    public void consumeProgramLifecycle(Map<String, Object> message) {
        System.out.println("🔔 FILE FeaturedFileKafkaConsumer (lifecycle) received: " + message);
        try {
            String type = (String) message.get("type");
            if (!"FEATURED_PROGRAM_DELETED".equals(type)) return;

            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) message.get("payload");
            if (payload == null) return;

            @SuppressWarnings("unchecked")
            List<Object> rawIds = (List<Object>) payload.get("sessionIds");
            if (rawIds == null || rawIds.isEmpty()) return;

            List<Long> sessionIds = rawIds.stream()
                    .map(o -> Long.valueOf(o.toString()))
                    .toList();

            service.deleteBySessionIds(sessionIds);

        } catch (Exception e) {
            System.out.println("❌ FeaturedFileKafkaConsumer (lifecycle) error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}