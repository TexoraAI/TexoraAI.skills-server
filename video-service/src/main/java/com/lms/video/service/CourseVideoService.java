//package com.lms.video.service;
//
//import com.lms.video.model.CourseVideo;
//import com.lms.video.repository.CourseVideoRepository;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
//@Service
//public class CourseVideoService {
//
//    private final CourseVideoRepository repo;
//
//    private static final String VIDEO_DIR =
//            System.getProperty("user.dir") + "/videos/course-content/";
//
//    public CourseVideoService(CourseVideoRepository repo) {
//        this.repo = repo;
//    }
//
//    public CourseVideo upload(
//            MultipartFile file,
//            Long courseId,
//            Long moduleId,
//            Long batchId,
//            String email
//    ) throws IOException {
//
//        File directory = new File(UPLOAD_DIR);
//        if (!directory.exists()) {
//            directory.mkdirs();
//        }
//
//        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
//        Path path = Paths.get(UPLOAD_DIR + fileName);
//
//        Files.copy(file.getInputStream(), path);
//
//        CourseVideo video = new CourseVideo();
//        video.setCourseId(courseId);
//        video.setModuleId(moduleId);
//        video.setBatchId(batchId);
//        video.setFileName(fileName);
//        video.setUrl("http://localhost:9000/videos/course-content/" + fileName);
//        video.setUploadedBy(email);
//
//        return repo.save(video);
//    }
//
//    public void deleteById(Long id) {
//
//        CourseVideo video = repo.findById(id)
//                .orElseThrow(() -> new RuntimeException("Course video not found"));
//
//        File file = new File(UPLOAD_DIR + video.getFileName());
//        if (file.exists()) {
//            file.delete();
//        }
//
//        repo.delete(video);
//    }
//}

package com.lms.video.service;

import com.lms.video.model.CourseVideo;
import com.lms.video.repository.CourseVideoRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class CourseVideoService {

    private final CourseVideoRepository repo;

    // ✅ SINGLE SOURCE OF TRUTH FOR DIRECTORY
    private static final String VIDEO_DIR =
            System.getProperty("user.dir") + "/videos/course-content/";

    public CourseVideoService(CourseVideoRepository repo) {
        this.repo = repo;
    }

    public CourseVideo upload(
            MultipartFile file,
            Long courseId,
            Long moduleId,
            Long batchId,
            String email
    ) throws IOException {

        // ✅ Ensure directory exists
        File directory = new File(VIDEO_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        Path path = Paths.get(VIDEO_DIR + fileName);

        Files.copy(file.getInputStream(), path);

        CourseVideo video = new CourseVideo();
        video.setCourseId(courseId);
        video.setModuleId(moduleId);
        video.setBatchId(batchId);
        video.setFileName(fileName);

        // keep URL same as before
        video.setUrl("http://localhost:9000/videos/course-content/" + fileName);

        video.setUploadedBy(email);

        return repo.save(video);
    }

    public void deleteById(Long id) {

        CourseVideo video = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Course video not found"));

        File file = new File(VIDEO_DIR + video.getFileName());
        if (file.exists()) {
            file.delete();
        }

        repo.delete(video);
    }
}