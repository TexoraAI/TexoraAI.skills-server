package com.lms.course.controller;

import com.lms.course.dto.*;
import com.lms.course.service.FeaturedProgramService;
import com.lms.course.service.OpenAIService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course/v1/featurecourse")

public class FeaturedProgramController {

    private final FeaturedProgramService featuredProgramService;
    private final OpenAIService openAIService;

    public FeaturedProgramController(FeaturedProgramService featuredProgramService,
                                      OpenAIService openAIService) {
        this.featuredProgramService = featuredProgramService;
        this.openAIService = openAIService;
    }

    // ===================== PUBLIC ENDPOINTS =====================

    // GET /api/course/v1/featurecourse
    @GetMapping
    public ResponseEntity<List<FeaturedProgramResponseDTO>> getAllActivePrograms() {
        return ResponseEntity.ok(featuredProgramService.getAllActivePrograms());
    }

    // GET /api/course/v1/featurecourse/category/{category}
    @GetMapping("/category/{category}")
    public ResponseEntity<List<FeaturedProgramResponseDTO>> getProgramsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(featuredProgramService.getProgramsByCategory(category));
    }

    // GET /api/course/v1/featurecourse/{id}
    @GetMapping("/{id}")
    public ResponseEntity<FeaturedProgramResponseDTO> getProgramById(@PathVariable Long id) {
        return ResponseEntity.ok(featuredProgramService.getProgramById(id));
    }

    // GET /api/course/v1/featurecourse/{id}/syllabus
    @GetMapping("/{id}/syllabus")
    public ResponseEntity<List<SyllabusWeekDto>> getProgramSyllabus(@PathVariable Long id) {
        return ResponseEntity.ok(featuredProgramService.getProgramSyllabus(id));
    }

    

    // ===================== SUPERADMIN ENDPOINTS =====================

    // GET /api/course/v1/featurecourse/superadmin
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/superadmin")
    public ResponseEntity<List<FeaturedProgramResponseDTO>> getAllProgramsForAdmin() {
        return ResponseEntity.ok(featuredProgramService.getAllProgramsForAdmin());
    }

    // GET /api/course/v1/featurecourse/superadmin/stats
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/superadmin/stats")
    public ResponseEntity<AdminStatsDTO> getAdminStats() {
        return ResponseEntity.ok(featuredProgramService.getAdminStats());
    }

    // POST /api/course/v1/featurecourse/superadmin
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/superadmin")
    public ResponseEntity<FeaturedProgramResponseDTO> createProgram(@Valid @RequestBody FeaturedProgramRequestDTO dto) {
        FeaturedProgramResponseDTO created = featuredProgramService.createProgram(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // PUT /api/course/v1/featurecourse/superadmin/{id}
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/superadmin/{id}")
    public ResponseEntity<FeaturedProgramResponseDTO> updateProgram(@PathVariable Long id,
                                                                      @Valid @RequestBody FeaturedProgramRequestDTO dto) {
        return ResponseEntity.ok(featuredProgramService.updateProgram(id, dto));
    }

    // DELETE /api/course/v1/featurecourse/superadmin/{id}
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/superadmin/{id}")
    public ResponseEntity<Void> deleteProgram(@PathVariable Long id) {
        featuredProgramService.deleteProgram(id);
        return ResponseEntity.noContent().build();
    }

    // POST /api/course/v1/featurecourse/superadmin/ai-generate
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/superadmin/ai-generate")
    public ResponseEntity<FeaturedProgramRequestDTO> generateWithAI(@Valid @RequestBody AIGenerateRequestDTO request) {
        FeaturedProgramRequestDTO generated = openAIService.generateProgramContent(
                request.getTopic(), request.getCategory(), request.getLevel());
        return ResponseEntity.ok(generated);
    }
}