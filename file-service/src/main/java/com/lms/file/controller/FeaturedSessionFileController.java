package com.lms.file.controller;

import com.lms.file.model.FeaturedSessionFile;
import com.lms.file.service.FeaturedSessionFileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

@RestController
@RequestMapping("/api/featured-files")
public class FeaturedSessionFileController {

    private final FeaturedSessionFileService service;

    public FeaturedSessionFileController(FeaturedSessionFileService service) {
        this.service = service;
    }

    // ================= UPLOAD =================
    // ✅ NEW: courseSlug param, so the file lands in featured-courses/{slug}/files/
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/upload")
    public FeaturedSessionFile upload(
            @RequestParam MultipartFile file,
            @RequestParam Long sessionId,
            @RequestParam(required = false) String courseSlug
    ) {
        return service.upload(file, sessionId, courseSlug);
    }

    // ================= STREAM — redirect to fresh presigned S3 URL =================
    // ✅ Replaces the old local-disk /download/{fileName} endpoint.
    // Works inside an <iframe src="..."> exactly like video's /stream does —
    // browser follows the 302 to S3 transparently.
    @GetMapping("/stream/{fileName:.+}")
    public ResponseEntity<Void> stream(@PathVariable String fileName) {
        String presignedUrl = service.getPlaybackUrl(fileName);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(presignedUrl))
                .build();
    }
}