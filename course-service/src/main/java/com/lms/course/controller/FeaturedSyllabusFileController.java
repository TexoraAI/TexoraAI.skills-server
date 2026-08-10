package com.lms.course.controller;

import com.lms.course.model.SyllabusSession;
import com.lms.course.service.FeaturedSyllabusFileService;
import org.springframework.web.bind.annotation.*;

@RestController
//@RequestMapping("/api/featured-programs/session")
@RequestMapping("/api/course/v1/featurecourse/superadmin/session")
public class FeaturedSyllabusFileController {

    private final FeaturedSyllabusFileService service;

    public FeaturedSyllabusFileController(FeaturedSyllabusFileService service) {
        this.service = service;
    }

    @PostMapping("/{sessionId}/file")
    public SyllabusSession markProcessing(@PathVariable Long sessionId) {
        return service.markProcessing(sessionId);
    }

    @GetMapping("/{sessionId}/file")
    public SyllabusSession getFile(@PathVariable Long sessionId) {
        return service.getFile(sessionId);
    }
    // ── NEW: matches the video feature's /video-status pattern ──
    // Frontend polls this endpoint every ~3s while fileStatus is PROCESSING
    @GetMapping("/{sessionId}/file-status")
    public SyllabusSession getFileStatus(@PathVariable Long sessionId) {
        return service.getFile(sessionId);
    }
    @DeleteMapping("/{sessionId}/file")
    public SyllabusSession deleteFile(@PathVariable Long sessionId) {
        return service.deleteFile(sessionId);
    }
}