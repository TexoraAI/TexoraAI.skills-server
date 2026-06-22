//package com.lms.file.kafka;
//
//import com.lms.file.event.CourseLifecycleEvent;
//import com.lms.file.model.CourseFile;
//import com.lms.file.repository.CourseFileRepository;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Service;
//
//import java.io.File;
//import java.util.List;
//
//@Service
//public class CourseDeleteConsumer {
//
//    private final CourseFileRepository repo;
//
//    private static final String FILE_DIR =
//            System.getProperty("user.dir") + "/files/course-content/";
//
//    public CourseDeleteConsumer(CourseFileRepository repo) {
//        this.repo = repo;
//    }
//
//    @KafkaListener(
//            topics = "course-lifecycle",
//            groupId = "file-service-group"
//    )
//    public void consume(CourseLifecycleEvent event) {
//
//        Long courseId = event.getCourseId();
//
//        switch (event.getType()) {
//
//            case "COURSE_DELETED" -> {
//
//                List<CourseFile> files = repo.findByCourseId(courseId);
//
//                for (CourseFile file : files) {
//
//                    File physical = new File(FILE_DIR + file.getFileName());
//                    if (physical.exists()) {
//                        physical.delete();
//                    }
//
//                    repo.delete(file);
//                }
//
//                System.out.println("🧹 FILE SERVICE cleaned for course " + courseId);
//            }
//
//            case "COURSE_UPDATED" -> {
//                System.out.println("🔄 FILE SERVICE received COURSE_UPDATED for " + courseId);
//            }
//        }
//    }
//}



package com.lms.file.kafka;

import com.lms.file.model.CourseFile;
import com.lms.file.repository.CourseFileRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;

@Service
public class CourseDeleteConsumer {

    private final CourseFileRepository repo;

    private static final String FILE_DIR =
            System.getProperty("user.dir") + "/files/course-content/";

    public CourseDeleteConsumer(CourseFileRepository repo) {
        this.repo = repo;
    }

    // ── FIXED: changed from CourseLifecycleEvent to Map<String, Object> ──────
    // Both video-service and file-service now use the same deserialization
    // strategy so neither silently fails on the course-lifecycle topic.
    @KafkaListener(
            topics  = "course-lifecycle",
            groupId = "file-service-group"
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

                // ── whole course deleted — clean every file for that course ───
                case "COURSE_DELETED" -> {
                    List<CourseFile> files = repo.findByCourseId(courseId);
                    if (files.isEmpty()) {
                        System.out.println("ℹ️ No files found for courseId=" + courseId);
                        return;
                    }
                    for (CourseFile courseFile : files) {
                        deleteFileFromDisk(courseFile.getFileName());
                        repo.delete(courseFile);
                    }
                    System.out.println("🧹 FILE SERVICE cleaned "
                            + files.size() + " file(s) for courseId=" + courseId);
                }

                case "COURSE_UPDATED" ->
                    System.out.println("🔄 FILE SERVICE received COURSE_UPDATED for courseId=" + courseId);

                default ->
                    System.out.println("ℹ️ FILE SERVICE ignoring course event type=" + type);
            }

        } catch (Exception e) {
            System.out.println("❌ FILE CourseDeleteConsumer error: " + e.getMessage());
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