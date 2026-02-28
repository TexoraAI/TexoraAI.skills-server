package com.lms.file.service;

import com.lms.file.model.CourseFile;
import com.lms.file.repository.CourseFileRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

@Service
public class CourseFileService {

    private final CourseFileRepository repo;

    // ✅ file-service storage folder
    private static final String FILE_DIR =
            System.getProperty("user.dir") + "/files/course-content/";

    public CourseFileService(CourseFileRepository repo) {
        this.repo = repo;
    }

    public CourseFile upload(
            MultipartFile file,
            Long courseId,
            Long moduleId,
            Long batchId,
            String email
    ) throws IOException {

        File directory = new File(FILE_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fileName =
                System.currentTimeMillis() + "_" + file.getOriginalFilename();

        Path path = Paths.get(FILE_DIR + fileName);

        Files.copy(file.getInputStream(), path);

        CourseFile courseFile = new CourseFile();

        courseFile.setCourseId(courseId);
        courseFile.setModuleId(moduleId);
        courseFile.setBatchId(batchId);
        courseFile.setFileName(fileName);
        courseFile.setUrl("http://localhost:9000/api/course-files/download/" + fileName);
        courseFile.setUploadedBy(email);
        return repo.save(courseFile);
    }

    public void deleteById(Long id) {

        CourseFile file = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Course file not found"));

        File diskFile = new File(FILE_DIR + file.getFileName());
        if (diskFile.exists()) {
            diskFile.delete();
        }

        repo.delete(file);
    }
}