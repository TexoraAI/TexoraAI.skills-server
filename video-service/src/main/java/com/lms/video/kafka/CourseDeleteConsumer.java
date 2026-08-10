

package com.lms.video.kafka;

import com.lms.video.model.CourseVideo;
import com.lms.video.repository.CourseVideoRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;

@Service
public class CourseDeleteConsumer {

    private final CourseVideoRepository repo;

    private static final String VIDEO_DIR =
            System.getProperty("user.dir") + "/videos/course-content/";

    public CourseDeleteConsumer(CourseVideoRepository repo) {
        this.repo = repo;
    }

    @KafkaListener(
            topics  = "course-lifecycle",
            groupId = "video-service-group"
    )
    public void consume(Map<String, Object> payload) {
        try {
            String type = (String) payload.get("type");
            if (type == null) {
                System.out.println("⚠️ course-lifecycle event has no type, skipping");
                return;
            }

            Long courseId = Long.valueOf(payload.get("courseId").toString());

            switch (type) {

                // ── whole course deleted — clean every video for that course ──
                case "COURSE_DELETED" -> {
                    List<CourseVideo> videos = repo.findByCourseId(courseId);
                    if (videos.isEmpty()) {
                        System.out.println("ℹ️ No videos found for courseId=" + courseId);
                        return;
                    }
                    for (CourseVideo video : videos) {
                        deleteFileFromDisk(video.getFileName());
                        repo.delete(video);
                    }
                    System.out.println("🧹 VIDEO SERVICE cleaned "
                            + videos.size() + " video(s) for courseId=" + courseId);
                }

                case "COURSE_UPDATED" ->
                    System.out.println("🔄 VIDEO SERVICE received COURSE_UPDATED for courseId=" + courseId);

                default ->
                    System.out.println("ℹ️ VIDEO SERVICE ignoring course event type=" + type);
            }

        } catch (Exception e) {
            System.out.println("❌ VIDEO CourseDeleteConsumer error: " + e.getMessage());
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