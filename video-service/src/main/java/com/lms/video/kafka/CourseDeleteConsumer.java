

package com.lms.video.kafka;

import com.lms.video.model.CourseVideo;
import com.lms.video.model.TranscriptSourceType;
import com.lms.video.repository.FeaturedVideoTranscriptRepository;
import com.lms.video.repository.FeaturedTranscriptSegmentRepository;
import com.lms.video.repository.CourseVideoRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;

@Service
public class CourseDeleteConsumer {

    private final CourseVideoRepository repo;
    private final FeaturedVideoTranscriptRepository transcriptRepo;
    private final FeaturedTranscriptSegmentRepository segmentRepo;
    private static final String VIDEO_DIR =
            System.getProperty("user.dir") + "/videos/course-content/";

    public CourseDeleteConsumer(CourseVideoRepository repo,
            FeaturedVideoTranscriptRepository transcriptRepo,
            FeaturedTranscriptSegmentRepository segmentRepo) {
        this.repo = repo;
        this.transcriptRepo = transcriptRepo;
        this.segmentRepo = segmentRepo;
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
//                    for (CourseVideo video : videos) {
//                        deleteFileFromDisk(video.getFileName());
//                        repo.delete(video);
//                    }
                    for (CourseVideo video : videos) {
                        deleteFileFromDisk(video.getFileName());
                        Long videoId = video.getId();
                        repo.delete(video);
                        transcriptRepo.findBySessionIdAndSourceType(videoId, TranscriptSourceType.COURSE_VIDEO)
                                .ifPresent(t -> {
                                    segmentRepo.deleteAll(segmentRepo.findByTranscriptIdOrderByOrderIndexAsc(t.getId()));
                                    transcriptRepo.delete(t);
                                });
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