package com.lms.course.controller;

import com.lms.course.dto.MentorFeedbackRequest;
import com.lms.course.dto.MentorFeedbackResponse;
import com.lms.course.dto.MentorFeedbackStatsResponse;
import com.lms.course.dto.PageResponse;
import com.lms.course.service.MentorFeedbackService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mentor-feedback")
public class MentorFeedbackController {

    private final MentorFeedbackService service;

    @Autowired
    public MentorFeedbackController(MentorFeedbackService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<MentorFeedbackResponse> create(@Valid @RequestBody MentorFeedbackRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MentorFeedbackResponse> update(
            @PathVariable Long id, @Valid @RequestBody MentorFeedbackRequest request
    ) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MentorFeedbackResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<MentorFeedbackResponse>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "all") String status,
            @RequestParam(required = false) Integer rating,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.getAll(search, status, rating, page, size));
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<MentorFeedbackResponse> toggleStatus(@PathVariable Long id) {
        return ResponseEntity.ok(service.toggleStatus(id));
    }

    @PatchMapping("/{id}/toggle-featured")
    public ResponseEntity<MentorFeedbackResponse> toggleFeatured(@PathVariable Long id) {
        return ResponseEntity.ok(service.toggleFeatured(id));
    }

    @GetMapping("/stats")
    public ResponseEntity<MentorFeedbackStatsResponse> getStats() {
        return ResponseEntity.ok(service.getStats());
    }

    @GetMapping("/public/active")
    public ResponseEntity<List<MentorFeedbackResponse>> getActiveForLandingPage() {
        return ResponseEntity.ok(service.getActiveForLandingPage());
    }
}