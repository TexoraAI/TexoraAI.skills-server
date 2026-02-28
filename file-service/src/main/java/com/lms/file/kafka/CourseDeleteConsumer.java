package com.lms.file.kafka;

import com.lms.file.event.CourseLifecycleEvent;
import com.lms.file.model.CourseFile;
import com.lms.file.repository.CourseFileRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class CourseDeleteConsumer {

    private final CourseFileRepository repo;

    private static final String FILE_DIR =
            System.getProperty("user.dir") + "/files/course-content/";

    public CourseDeleteConsumer(CourseFileRepository repo) {
        this.repo = repo;
    }

    @KafkaListener(
            topics = "course-lifecycle",
            groupId = "file-service-group"
    )
    public void consume(CourseLifecycleEvent event) {

        Long courseId = event.getCourseId();

        switch (event.getType()) {

            case "COURSE_DELETED" -> {

                List<CourseFile> files = repo.findByCourseId(courseId);

                for (CourseFile file : files) {

                    File physical = new File(FILE_DIR + file.getFileName());
                    if (physical.exists()) {
                        physical.delete();
                    }

                    repo.delete(file);
                }

                System.out.println("🧹 FILE SERVICE cleaned for course " + courseId);
            }

            case "COURSE_UPDATED" -> {
                System.out.println("🔄 FILE SERVICE received COURSE_UPDATED for " + courseId);
            }
        }
    }
}