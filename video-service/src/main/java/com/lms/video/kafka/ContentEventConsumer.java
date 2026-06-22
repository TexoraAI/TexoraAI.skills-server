package com.lms.video.kafka;

import com.lms.video.repository.CourseVideoRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Map;

@Service
public class ContentEventConsumer {

    private final CourseVideoRepository repo;

    private static final String VIDEO_DIR =
            System.getProperty("user.dir") + "/videos/course-content/";

    public ContentEventConsumer(CourseVideoRepository repo) {
        this.repo = repo;
    }

    @KafkaListener(
            topics  = "${topics.content}",
            groupId = "video-service-group"
    )
    public void consume(Map<String, Object> message) {
    	System.out.println("🔔 VIDEO ContentEventConsumer received: " + message);
        try {
            String type = (String) message.get("type");
            if (type == null) {
                System.out.println("⚠️ content-events: type is null, skipping");
                return;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) message.get("payload");
            if (payload == null) {
                System.out.println("⚠️ content-events: payload is null, skipping");
                return;
            }

            String contentType = (String) payload.get("contentType");

            // this consumer only handles VIDEO
            if (!"VIDEO".equals(contentType)) return;

            switch (type) {

                case "CONTENT_DELETED" -> {
                    String url = (String) payload.get("url");
                    if (url == null || url.isBlank()) {
                        System.out.println("⚠️ CONTENT_DELETED: url is null");
                        return;
                    }
                    repo.findByUrl(url).ifPresentOrElse(
                        courseVideo -> {
                            deleteFileFromDisk(courseVideo.getFileName());
                            repo.delete(courseVideo);
                            System.out.println("🧹 VIDEO deleted → url=" + url);
                        },
                        () -> System.out.println("⚠️ VIDEO not found for url=" + url)
                    );
                }

                case "CONTENT_UPDATED" -> {
                    String oldUrl = (String) payload.get("oldUrl");
                    String newUrl = (String) payload.get("newUrl");
                    if (oldUrl == null || oldUrl.isBlank()) {
                        System.out.println("⚠️ CONTENT_UPDATED: oldUrl is null");
                        return;
                    }
                    if (oldUrl.equals(newUrl)) {
                        System.out.println("ℹ️ CONTENT_UPDATED: oldUrl == newUrl, nothing to clean");
                        return;
                    }
                    repo.findByUrl(oldUrl).ifPresentOrElse(
                        courseVideo -> {
                            deleteFileFromDisk(courseVideo.getFileName());
                            repo.delete(courseVideo);
                            System.out.println("🧹 VIDEO old file cleaned → oldUrl=" + oldUrl);
                        },
                        () -> System.out.println("⚠️ VIDEO not found for oldUrl=" + oldUrl)
                    );
                }

                default -> System.out.println("ℹ️ VIDEO ignoring content event type=" + type);
            }

        } catch (Exception e) {
            System.out.println("❌ VIDEO ContentEventConsumer error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── helper ───────────────────────────────────────────────────────────────
    private void deleteFileFromDisk(String fileName) {
        if (fileName == null || fileName.isBlank()) return;
        File file = new File(VIDEO_DIR + fileName);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                System.out.println("⚠️ Could not delete video from disk: " + fileName);
            }
        } else {
            System.out.println("⚠️ Video file not found on disk (already gone?): " + fileName);
        }
    }
}