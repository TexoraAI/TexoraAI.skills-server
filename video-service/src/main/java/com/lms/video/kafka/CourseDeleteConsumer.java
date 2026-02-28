package com.lms.video.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.video.event.CourseLifecycleEvent;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String VIDEO_DIR =
            System.getProperty("user.dir") + "/videos/course-content/";

    public CourseDeleteConsumer(CourseVideoRepository repo) {
        this.repo = repo;
    }
    @KafkaListener(
            topics = "course-lifecycle",
            groupId = "video-service-group"
    )
    public void consume(Map<String, Object> payload) {

        try {

            String type = (String) payload.get("type");
            Long courseId = Long.valueOf(payload.get("courseId").toString());

            if ("COURSE_DELETED".equals(type)) {

                List<CourseVideo> videos = repo.findByCourseId(courseId);

                for (CourseVideo video : videos) {

                    File file = new File(VIDEO_DIR + video.getFileName());
                    if (file.exists()) {
                        file.delete();
                    }

                    repo.delete(video);
                }

                System.out.println("🧹 VIDEO SERVICE cleaned for course " + courseId);
            }

            if ("COURSE_UPDATED".equals(type)) {
                System.out.println("🔄 VIDEO SERVICE received COURSE_UPDATED for " + courseId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}