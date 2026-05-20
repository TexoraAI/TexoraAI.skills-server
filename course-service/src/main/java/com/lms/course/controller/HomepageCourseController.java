package com.lms.course.controller;

import com.lms.course.dto.HomepageCourseDto;
import com.lms.course.service.HomepageCourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/homepage/courses")
public class HomepageCourseController {

    private final HomepageCourseService service;

    public HomepageCourseController(HomepageCourseService service) {
        this.service = service;
    }

    // ----------------------------------------------------------------
    // PUBLIC ENDPOINTS
    // ----------------------------------------------------------------

    @GetMapping
    public ResponseEntity<List<HomepageCourseDto>> getHomepageCourses() {
        return ResponseEntity.ok(service.getHomepageCourses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HomepageCourseDto> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getCourseById(id));
    }

    @GetMapping("/{id}/syllabus")
    public ResponseEntity<String> getSyllabus(@PathVariable Long id) {
        return ResponseEntity.ok(service.getSyllabus(id));
    }

    // ----------------------------------------------------------------
    // ADMIN ENDPOINTS
    // ----------------------------------------------------------------

    @PostMapping
    public ResponseEntity<HomepageCourseDto> createCourse(@RequestBody HomepageCourseDto dto) {
        return ResponseEntity.ok(service.createCourse(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HomepageCourseDto> updateCourse(
            @PathVariable Long id,
            @RequestBody HomepageCourseDto dto) {
        return ResponseEntity.ok(service.updateCourse(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> softDeleteCourse(@PathVariable Long id) {
        service.softDeleteCourse(id);
        return ResponseEntity.ok("Course with id " + id + " has been deleted successfully.");
    }

    @PutMapping("/{id}/syllabus")
    public ResponseEntity<HomepageCourseDto> updateSyllabus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String syllabusJson = body.get("syllabusJson");
        return ResponseEntity.ok(service.updateSyllabus(id, syllabusJson));
    }

    // ----------------------------------------------------------------
    // AI ENDPOINTS
    // ----------------------------------------------------------------

    @PostMapping("/{id}/syllabus/ai-generate")
    public ResponseEntity<HomepageCourseDto> generateSyllabusWithAi(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String rawText = body.get("rawText");
        return ResponseEntity.ok(service.generateSyllabusWithAi(id, rawText));
    }

    @PostMapping("/{id}/syllabus/upload")
    public ResponseEntity<HomepageCourseDto> uploadSyllabusAndGenerate(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(service.uploadSyllabusAndGenerate(id, file));
    }
}