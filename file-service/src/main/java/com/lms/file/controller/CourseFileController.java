



package com.lms.file.controller;

import com.lms.file.model.CourseFile;
import com.lms.file.service.CourseFileService;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

@RestController
@RequestMapping("/api/course-files")
public class CourseFileController {

    private final CourseFileService service;

    private static final String FILE_DIR =
            System.getProperty("user.dir") + "/files/course-content/";

    public CourseFileController(CourseFileService service) {
        this.service = service;
    }

    // ================= UPLOAD =================
    @PostMapping("/upload")
    public CourseFile upload(
            @RequestParam MultipartFile file,
            @RequestParam Long courseId,
            @RequestParam Long moduleId,
            @RequestParam Long batchId,
            Authentication auth
    ) throws IOException {
        return service.upload(file, courseId, moduleId, batchId, auth.getName());
    }

    // ================= EDIT =================
    // Accepts multipart/form-data — replacement file is optional.
    // Frontend sends: file (optional), courseId, moduleId, batchId
    @PutMapping("/{id}")
    public CourseFile update(
            @PathVariable Long id,
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long moduleId,
            @RequestParam(required = false) Long batchId,
            Authentication auth
    ) throws IOException {
        return service.update(
                id,
                file,
                courseId,
                moduleId,
                batchId,
                auth != null ? auth.getName() : null
        );
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.ok("Course file deleted successfully");
    }

    // ================= SECURED DOWNLOAD =================
    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> download(
            @PathVariable String fileName,
            Authentication auth
    ) throws IOException {

        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        File file = new File(FILE_DIR + fileName);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(file.length())
                .body(resource);
    }
}