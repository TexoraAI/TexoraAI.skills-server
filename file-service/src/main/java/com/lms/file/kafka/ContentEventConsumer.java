package com.lms.file.kafka;

import com.lms.file.repository.CourseFileRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Map;

@Service
public class ContentEventConsumer {

    private final CourseFileRepository repo;

    private static final String FILE_DIR =
            System.getProperty("user.dir") + "/files/course-content/";

    public ContentEventConsumer(CourseFileRepository repo) {
        this.repo = repo;
    }

    @KafkaListener(
            topics  = "${topics.content}",
            groupId = "file-service-group"
    )
    public void consume(Map<String, Object> message) {
    	System.out.println("🔔 file ContentEventConsumer received: " + message);
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

            // this consumer only handles PDF
            if (!"PDF".equals(contentType)) return;

            switch (type) {

                case "CONTENT_DELETED" -> {
                    String url = (String) payload.get("url");
                    if (url == null || url.isBlank()) {
                        System.out.println("⚠️ CONTENT_DELETED: url is null");
                        return;
                    }
                    repo.findByUrl(url).ifPresentOrElse(
                        courseFile -> {
                            deleteFileFromDisk(courseFile.getFileName());
                            repo.delete(courseFile);
                            System.out.println("🧹 FILE deleted → url=" + url);
                        },
                        () -> System.out.println("⚠️ FILE not found for url=" + url)
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
                        courseFile -> {
                            deleteFileFromDisk(courseFile.getFileName());
                            repo.delete(courseFile);
                            System.out.println("🧹 FILE old file cleaned → oldUrl=" + oldUrl);
                        },
                        () -> System.out.println("⚠️ FILE not found for oldUrl=" + oldUrl)
                    );
                }

                default -> System.out.println("ℹ️ FILE ignoring content event type=" + type);
            }

        } catch (Exception e) {
            System.out.println("❌ FILE ContentEventConsumer error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── helper ───────────────────────────────────────────────────────────────
    private void deleteFileFromDisk(String fileName) {
        if (fileName == null || fileName.isBlank()) return;
        File file = new File(FILE_DIR + fileName);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                System.out.println("⚠️ Could not delete file from disk: " + fileName);
            }
        } else {
            System.out.println("⚠️ File not found on disk (already gone?): " + fileName);
        }
    }
}