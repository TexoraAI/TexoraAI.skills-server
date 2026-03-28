package com.lms.video.controller;

import com.lms.video.dto.UploadCourseDTO;
import com.lms.video.model.UploadCourse;
import com.lms.video.service.UploadCourseService;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/upload-course")
public class UploadCourseController {

    private final UploadCourseService service;

    // 📁 SAME FOLDER USED IN SERVICE
    private static final String VIDEO_DIR =
            System.getProperty("user.dir") + "/videos/course-content/";
    public UploadCourseController(UploadCourseService service) {
        this.service = service;
    }

    // ================= UPLOAD =================
    @PostMapping("/upload")
    public UploadCourse upload(
            @RequestPart("video") MultipartFile video,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @ModelAttribute UploadCourseDTO dto
    ) throws IOException {

        return service.upload(video, thumbnail, dto);
    }

    // ================= DELETE =================
    @DeleteMapping("/by-course/{courseId}")
    public String deleteByCourse(@PathVariable Long courseId) {
        service.deleteByCourseId(courseId);
        return "UploadCourse deleted for courseId: " + courseId;
    }

    // ================= ✅ GET ALL (NEW) =================
    @GetMapping("/all")
    public List<UploadCourse> getAll() {
        return service.getAll();
    }

    // ================= ✅ STREAM VIDEO (NEW) =================
    @GetMapping("/stream/{fileName:.+}")
    public ResponseEntity<Resource> streamVideo(@PathVariable String fileName) {

        File file = new File(VIDEO_DIR + fileName);

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .header("Content-Type", "video/mp4")
                .body(resource);
    }
}