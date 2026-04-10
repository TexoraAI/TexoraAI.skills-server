
package com.lms.progress.controller;

import com.lms.progress.dto.ProgressRequest;
import com.lms.progress.dto.ProgressResponse;
import com.lms.progress.service.ProgressService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    private final ProgressService service;

    public ProgressController(ProgressService service) {
        this.service = service;
    }

    @PostMapping
    public ProgressResponse create(@RequestBody ProgressRequest req) {
        return service.create(req);
    }

    @GetMapping("/{id}")
    public ProgressResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/user")
    public ProgressResponse getByUserAndCourse(
            @RequestParam String email,
            @RequestParam Long courseId) {
        return service.getByUserAndCourse(email, courseId);
    }

    @PutMapping("/{id}")
    public ProgressResponse update(@PathVariable Long id,
                                   @RequestBody ProgressRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/user")
    public void deleteByEmail(@RequestParam String email) {
        service.deleteByEmail(email);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id) {
        service.deleteById(id);
    }

    // ============================
    // MARK CONTENT AS COMPLETE
    // Frontend calls this when student opens a video or PDF
    // This is the DIRECT REST path (no Kafka needed from frontend)
    // ============================
    @PostMapping("/mark-complete")
    public ProgressResponse markComplete(
            @RequestParam String email,
            @RequestParam Long   courseId,
            @RequestParam Long   contentId,
            @RequestParam int    totalContentCount) {

        return service.markContentComplete(email, courseId, contentId, totalContentCount);
    }
}