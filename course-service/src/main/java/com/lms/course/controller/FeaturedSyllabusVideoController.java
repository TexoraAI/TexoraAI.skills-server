package com.lms.course.controller;

import com.lms.course.model.SyllabusSession;
import com.lms.course.service.FeaturedSyllabusVideoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/course/v1/featurecourse/superadmin")
public class FeaturedSyllabusVideoController {

    private final FeaturedSyllabusVideoService featuredSyllabusVideoService;

    public FeaturedSyllabusVideoController(FeaturedSyllabusVideoService featuredSyllabusVideoService) {
        this.featuredSyllabusVideoService = featuredSyllabusVideoService;
    }

    // POST /api/course/v1/featurecourse/superadmin/session/{sessionId}/video
    // NOTE: this no longer accepts the file. The frontend uploads the file directly to
    // video-service's POST /api/video/v1/featured/session, and calls this endpoint
    // alongside it (before or in parallel) just to flip the session to PROCESSING
    // immediately, ahead of the VIDEO_READY/FAILED Kafka message.
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/session/{sessionId}/video")
    public ResponseEntity<?> markSessionVideoUploadStarted(@PathVariable Long sessionId) {
        try {
            SyllabusSession session = featuredSyllabusVideoService.markUploadStarted(sessionId);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                    "sessionId", session.getId(),
                    "videoStatus", session.getVideoStatus(),
                    "message", "Marked as processing. Upload the file directly to video-service."
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // DELETE /api/course/v1/featurecourse/superadmin/session/{sessionId}/video
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/session/{sessionId}/video")
    public ResponseEntity<?> deleteSessionVideo(@PathVariable Long sessionId) {
        try {
            featuredSyllabusVideoService.deleteVideo(sessionId);
            return ResponseEntity.ok(Map.of("message", "Session video deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // GET /api/course/v1/featurecourse/superadmin/session/{sessionId}/video-status
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/session/{sessionId}/video-status")
    public ResponseEntity<?> getSessionVideoStatus(@PathVariable Long sessionId) {
        try {
            return ResponseEntity.ok(featuredSyllabusVideoService.getVideoStatus(sessionId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}