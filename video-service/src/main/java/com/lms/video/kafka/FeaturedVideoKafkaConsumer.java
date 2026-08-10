package com.lms.video.kafka;

import com.lms.video.model.FeaturedSessionVideo;
import com.lms.video.repository.FeaturedSessionVideoRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FeaturedVideoKafkaConsumer {

    private final FeaturedSessionVideoRepository repo;

    private static final String VIDEO_DIR =
            System.getProperty("user.dir") + "/videos/featured-content/";

    public FeaturedVideoKafkaConsumer(FeaturedSessionVideoRepository repo) {
        this.repo = repo;
    }

    // ── topics.featured-content: FEATURED_VIDEO_DELETED / FEATURED_VIDEO_UPDATED ──
    @KafkaListener(
            topics  = "${topics.featured-content}",
            groupId = "video-service-group"
    )
    public void consumeFeaturedContent(Map<String, Object> message) {
        System.out.println("🔔 VIDEO FeaturedVideoKafkaConsumer (featured-content) received: " + message);
        try {
            String type = (String) message.get("type");
            if (type == null) {
                System.out.println("⚠️ featured-content-events: type is null, skipping");
                return;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) message.get("payload");
            if (payload == null) {
                System.out.println("⚠️ featured-content-events: payload is null, skipping");
                return;
            }

            String contentType = (String) payload.get("contentType");
            if (!"VIDEO".equals(contentType)) return;

            switch (type) {

                case "FEATURED_VIDEO_DELETED" -> {
                    String oldUrl = (String) payload.get("oldUrl");
                    if (oldUrl == null || oldUrl.isBlank()) {
                        System.out.println("⚠️ FEATURED_VIDEO_DELETED: oldUrl is null");
                        return;
                    }
                    repo.findByUrl(oldUrl).ifPresentOrElse(
                        video -> {
                            deleteFileFromDisk(video.getFileName());
                            repo.delete(video);
                            System.out.println("🧹 FEATURED VIDEO deleted → url=" + oldUrl);
                        },
                        () -> System.out.println("⚠️ FEATURED VIDEO not found for url=" + oldUrl)
                    );
                }

                case "FEATURED_VIDEO_UPDATED" -> {
                    String oldUrl = (String) payload.get("oldUrl");
                    String newUrl = (String) payload.get("newUrl");
                    if (oldUrl == null || oldUrl.isBlank()) {
                        System.out.println("⚠️ FEATURED_VIDEO_UPDATED: oldUrl is null");
                        return;
                    }
                    if (oldUrl.equals(newUrl)) {
                        System.out.println("ℹ️ FEATURED_VIDEO_UPDATED: oldUrl == newUrl, nothing to clean");
                        return;
                    }
                    repo.findByUrl(oldUrl).ifPresentOrElse(
                        video -> {
                            deleteFileFromDisk(video.getFileName());
                            repo.delete(video);
                            System.out.println("🧹 FEATURED VIDEO old file cleaned → oldUrl=" + oldUrl);
                        },
                        () -> System.out.println("⚠️ FEATURED VIDEO not found for oldUrl=" + oldUrl)
                    );
                }

                default -> System.out.println("ℹ️ FEATURED VIDEO ignoring content event type=" + type);
            }

        } catch (Exception e) {
            System.out.println("❌ VIDEO FeaturedVideoKafkaConsumer (featured-content) error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── topics.featured-program-lifecycle: FEATURED_PROGRAM_DELETED ────────────
    @KafkaListener(
            topics  = "${topics.featured-program-lifecycle}",
            groupId = "video-service-group"
    )
    public void consumeFeaturedProgramLifecycle(Map<String, Object> message) {
        System.out.println("🔔 VIDEO FeaturedVideoKafkaConsumer (featured-program-lifecycle) received: " + message);
        try {
            String type = (String) message.get("type");
            if (type == null) {
                System.out.println("⚠️ featured-program-lifecycle: type is null, skipping");
                return;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) message.get("payload");
            if (payload == null) {
                System.out.println("⚠️ featured-program-lifecycle: payload is null, skipping");
                return;
            }

            switch (type) {

                case "FEATURED_PROGRAM_DELETED" -> {
                    Object sessionIdsRaw = payload.get("sessionIds");
                    if (!(sessionIdsRaw instanceof List<?> rawList) || rawList.isEmpty()) {
                        System.out.println("ℹ️ FEATURED_PROGRAM_DELETED: no sessionIds to clean");
                        return;
                    }

                    List<Long> sessionIds = rawList.stream()
                            .map(o -> Long.valueOf(o.toString()))
                            .collect(Collectors.toList());

                    List<FeaturedSessionVideo> videos = repo.findBySessionIdIn(sessionIds);
                    if (videos.isEmpty()) {
                        System.out.println("ℹ️ No featured videos found for sessionIds=" + sessionIds);
                        return;
                    }
                    for (FeaturedSessionVideo video : videos) {
                        deleteFileFromDisk(video.getFileName());
                        repo.delete(video);
                    }
                    System.out.println("🧹 VIDEO SERVICE cleaned " + videos.size()
                            + " featured video(s) for sessionIds=" + sessionIds);
                }

                default -> System.out.println("ℹ️ VIDEO SERVICE ignoring featured-program-lifecycle event type=" + type);
            }

        } catch (Exception e) {
            System.out.println("❌ VIDEO FeaturedVideoKafkaConsumer (featured-program-lifecycle) error: " + e.getMessage());
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
                System.out.println("⚠️ Could not delete featured video from disk: " + fileName);
            }
        } else {
            System.out.println("⚠️ Featured video file not found on disk (already gone?): " + fileName);
        }
    }
}