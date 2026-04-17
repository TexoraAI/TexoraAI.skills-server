


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

    private static final String FILE_DIR =
            System.getProperty("user.dir") + "/files/course-content/";

    public CourseFileService(CourseFileRepository repo) {
        this.repo = repo;
    }

    // ================= UPLOAD =================
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

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
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

    // ================= EDIT (replace file + keep metadata) =================
    public CourseFile update(
            Long id,
            MultipartFile newFile,   // nullable — if null, keep existing file
            Long courseId,
            Long moduleId,
            Long batchId,
            String email
    ) throws IOException {

        CourseFile existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Course file not found: " + id));

        if (newFile != null && !newFile.isEmpty()) {

            // 1. Delete old file from disk
            File oldFile = new File(FILE_DIR + existing.getFileName());
            if (oldFile.exists()) {
                oldFile.delete();
            }

            // 2. Ensure directory exists
            File directory = new File(FILE_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 3. Save new file
            String newFileName = System.currentTimeMillis() + "_" + newFile.getOriginalFilename();
            Path newPath = Paths.get(FILE_DIR + newFileName);
            Files.copy(newFile.getInputStream(), newPath);

            existing.setFileName(newFileName);
            existing.setUrl("http://localhost:9000/api/course-files/download/" + newFileName);
        }

        // Update metadata (only if non-null values are provided)
        if (courseId != null) existing.setCourseId(courseId);
        if (moduleId != null) existing.setModuleId(moduleId);
        if (batchId  != null) existing.setBatchId(batchId);
        if (email    != null) existing.setUploadedBy(email);

        return repo.save(existing);
    }

    // ================= DELETE =================
    public void deleteById(Long id) {

        CourseFile file = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Course file not found: " + id));

        File diskFile = new File(FILE_DIR + file.getFileName());
        if (diskFile.exists()) {
            diskFile.delete();
        }

        repo.delete(file);
    }
}