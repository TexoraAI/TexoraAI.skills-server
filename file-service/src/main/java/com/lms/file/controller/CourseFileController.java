
package com.lms.file.controller;

import com.lms.file.model.CourseFile;
import com.lms.file.service.CourseFileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/course-files")
public class CourseFileController {

    private final CourseFileService service;

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
    // CHANGED: now id-based (matches what `url` in the DB points to), and
    // redirects to a freshly-generated presigned S3 URL instead of streaming
    // bytes off local disk. Auth is still enforced before the redirect.
    // ================= SECURED DOWNLOAD =================
    // Returns JSON with a presigned S3 URL instead of a redirect/bytes,
    // for the same auth-header-on-redirect reason as FileController.
    @GetMapping("/download/{id}")
    public ResponseEntity<java.util.Map<String, String>> download(
            @PathVariable Long id,
            Authentication auth
    ) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        CourseFile file = service.getById(id);
        String url = service.getPresignedUrl(file.getFileName());
        return ResponseEntity.ok(java.util.Map.of("url", url));
    }
    // ================= USAGE (file course-content storage quota) =================
    @GetMapping("/upload-quota")
    public java.util.Map<String, Object> getUploadQuota(Authentication auth) {
        return service.getCourseFileUsage(auth.getName());
    }
}