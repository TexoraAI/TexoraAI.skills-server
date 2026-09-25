package com.lms.video.controller;

import com.lms.video.model.FeaturedSessionVideo;
import com.lms.video.service.FeaturedSessionVideoService;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;

@RestController
@RequestMapping("/api/video/v1/featured/session")
public class FeaturedSessionVideoController {

    private final FeaturedSessionVideoService service;

    public FeaturedSessionVideoController(FeaturedSessionVideoService service) {
        this.service = service;
    }

    // ================= UPLOAD =================
    // ✅ NEW: courseSlug param — determines the featured-courses/{slug}/... S3 folder
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping
    public FeaturedSessionVideo upload(
            @RequestParam MultipartFile file,
            @RequestParam Long sessionId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) MultipartFile thumbnail,
            @RequestParam(required = false) String courseSlug
    ) throws IOException {
        return service.upload(file, sessionId, title, description, thumbnail, courseSlug);
    }

    // ================= GET BY SESSION =================
    @GetMapping("/session/{sessionId}")
    public FeaturedSessionVideo getBySession(@PathVariable Long sessionId) {
        return service.getBySession(sessionId);
    }

    // ================= EDIT METADATA / REPLACE VIDEO / REPLACE THUMBNAIL =================
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FeaturedSessionVideo update(
            @PathVariable Long id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) MultipartFile thumbnail,
            @RequestParam(required = false) MultipartFile newVideo,
            @RequestParam(required = false) String courseSlug
    ) throws IOException {
        return service.update(id, title, description, thumbnail, newVideo, courseSlug);
    }

    // ================= DELETE =================
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    // ================= STREAM =================
    @GetMapping("/stream/{fileName:.+}")
    public ResponseEntity<Void> streamVideo(@PathVariable String fileName) {
        String presignedUrl = service.getPlaybackUrl(fileName);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(presignedUrl))
                .build();
    }
}