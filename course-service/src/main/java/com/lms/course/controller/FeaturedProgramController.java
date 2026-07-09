//package com.lms.course.controller;
//
//import com.lms.course.dto.*;
//import java.util.Map;
//import com.lms.course.service.FeaturedProgramService;
//import com.lms.course.service.FileTextExtractionService;
//import com.lms.course.service.OpenAIService;
//import jakarta.validation.Valid;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import org.springframework.web.multipart.MultipartFile;
//@RestController
//@RequestMapping("/api/course/v1/featurecourse")
//
//public class FeaturedProgramController {
//
//    private final FeaturedProgramService featuredProgramService;
//    private final OpenAIService openAIService;
//    private final FileTextExtractionService fileTextExtractionService;
//    public FeaturedProgramController(FeaturedProgramService featuredProgramService,
//                                      OpenAIService openAIService,
//                                      FileTextExtractionService fileTextExtractionService) {
//        this.featuredProgramService = featuredProgramService;
//        this.openAIService = openAIService;
//        this.fileTextExtractionService = fileTextExtractionService;
//        
//    }
//
//    // ===================== PUBLIC ENDPOINTS =====================
//
//    // GET /api/course/v1/featurecourse
//    @GetMapping
//    public ResponseEntity<List<FeaturedProgramResponseDTO>> getAllActivePrograms() {
//        return ResponseEntity.ok(featuredProgramService.getAllActivePrograms());
//    }
//
//    // GET /api/course/v1/featurecourse/category/{category}
//    @GetMapping("/category/{category}")
//    public ResponseEntity<List<FeaturedProgramResponseDTO>> getProgramsByCategory(@PathVariable String category) {
//        return ResponseEntity.ok(featuredProgramService.getProgramsByCategory(category));
//    }
//
//    // GET /api/course/v1/featurecourse/{id}
//    @GetMapping("/{id}")
//    public ResponseEntity<FeaturedProgramResponseDTO> getProgramById(@PathVariable Long id) {
//        return ResponseEntity.ok(featuredProgramService.getProgramById(id));
//    }
//
//    // GET /api/course/v1/featurecourse/{id}/syllabus
//    @GetMapping("/{id}/syllabus")
//    public ResponseEntity<List<SyllabusWeekDto>> getProgramSyllabus(@PathVariable Long id) {
//        return ResponseEntity.ok(featuredProgramService.getProgramSyllabus(id));
//    }
//
//    
//
//    // ===================== SUPERADMIN ENDPOINTS =====================
//
//    // GET /api/course/v1/featurecourse/superadmin
//    @PreAuthorize("hasRole('SUPER_ADMIN')")
//    @GetMapping("/superadmin")
//    public ResponseEntity<List<FeaturedProgramResponseDTO>> getAllProgramsForAdmin() {
//        return ResponseEntity.ok(featuredProgramService.getAllProgramsForAdmin());
//    }
//
//    // GET /api/course/v1/featurecourse/superadmin/stats
//    @PreAuthorize("hasRole('SUPER_ADMIN')")
//    @GetMapping("/superadmin/stats")
//    public ResponseEntity<AdminStatsDTO> getAdminStats() {
//        return ResponseEntity.ok(featuredProgramService.getAdminStats());
//    }
//
//    // POST /api/course/v1/featurecourse/superadmin
//    @PreAuthorize("hasRole('SUPER_ADMIN')")
//    @PostMapping("/superadmin")
//    public ResponseEntity<FeaturedProgramResponseDTO> createProgram(@Valid @RequestBody FeaturedProgramRequestDTO dto) {
//        FeaturedProgramResponseDTO created = featuredProgramService.createProgram(dto);
//        return ResponseEntity.status(HttpStatus.CREATED).body(created);
//    }
//
//    // PUT /api/course/v1/featurecourse/superadmin/{id}
//    @PreAuthorize("hasRole('SUPER_ADMIN')")
//    @PutMapping("/superadmin/{id}")
//    public ResponseEntity<FeaturedProgramResponseDTO> updateProgram(@PathVariable Long id,
//                                                                      @Valid @RequestBody FeaturedProgramRequestDTO dto) {
//        return ResponseEntity.ok(featuredProgramService.updateProgram(id, dto));
//    }
//
//    // DELETE /api/course/v1/featurecourse/superadmin/{id}
//    @PreAuthorize("hasRole('SUPER_ADMIN')")
//    @DeleteMapping("/superadmin/{id}")
//    public ResponseEntity<Void> deleteProgram(@PathVariable Long id) {
//        featuredProgramService.deleteProgram(id);
//        return ResponseEntity.noContent().build();
//    }
//
//    // POST /api/course/v1/featurecourse/superadmin/ai-generate
//    @PreAuthorize("hasRole('SUPER_ADMIN')")
//    @PostMapping("/superadmin/ai-generate")
//    public ResponseEntity<FeaturedProgramRequestDTO> generateWithAI(@Valid @RequestBody AIGenerateRequestDTO request) {
//        FeaturedProgramRequestDTO generated = openAIService.generateProgramContent(
//                request.getTopic(), request.getCategory(), request.getLevel());
//        return ResponseEntity.ok(generated);
//    }
// // NEW: PDF / DOC / DOCX -> structured syllabus (Upload File feature)
//    @PreAuthorize("hasRole('SUPER_ADMIN')")
//    @PostMapping(value = "/superadmin/syllabus/extract", consumes = "multipart/form-data")
//    public ResponseEntity<?> extractSyllabusFromFile(
//            @RequestParam("file") MultipartFile file) {
//        try {
//            if (file == null || file.isEmpty()) {
//                return ResponseEntity.badRequest().body(Map.of("message", "No file was received by the server."));
//            }
//
//            String text = fileTextExtractionService.extractText(file);
//            if (text == null || text.isBlank()) {
//                return ResponseEntity.badRequest().body(Map.of(
//                        "message", "Could not extract any text from this file. It may be a scanned/image-only PDF."));
//            }
//
//            List<ExtractedWeekDto> weeks = openAIService.generateSyllabusFromExtractedText(text);
//            return ResponseEntity.ok(weeks);
//
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError().body(Map.of(
//                    "message", "Syllabus extraction failed: " + e.getMessage()));
//        }
//    }
//}




package com.lms.course.controller;

import com.lms.course.dto.*;
import java.util.Map;
import com.lms.course.service.FeaturedProgramService;
import com.lms.course.service.FileTextExtractionService;
import com.lms.course.service.OpenAIService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
@RestController
@RequestMapping("/api/course/v1/featurecourse")

public class FeaturedProgramController {

    private final FeaturedProgramService featuredProgramService;
    private final OpenAIService openAIService;
    private final FileTextExtractionService fileTextExtractionService;
    public FeaturedProgramController(FeaturedProgramService featuredProgramService,
                                      OpenAIService openAIService,
                                      FileTextExtractionService fileTextExtractionService) {
        this.featuredProgramService = featuredProgramService;
        this.openAIService = openAIService;
        this.fileTextExtractionService = fileTextExtractionService;
        
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

    // PUT /api/course/v1/featurecourse/superadmin/{id}/publish
    // NEW: flips publishStatus to "Published" only, no other field changes
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/superadmin/{id}/publish")
    public ResponseEntity<FeaturedProgramResponseDTO> publishProgram(@PathVariable Long id) {
        return ResponseEntity.ok(featuredProgramService.publishProgram(id));
    }

    // POST /api/course/v1/featurecourse/superadmin/ai-generate
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/superadmin/ai-generate")
    public ResponseEntity<FeaturedProgramRequestDTO> generateWithAI(@Valid @RequestBody AIGenerateRequestDTO request) {
        FeaturedProgramRequestDTO generated = openAIService.generateProgramContent(
                request.getTopic(), request.getCategory(), request.getLevel());
        return ResponseEntity.ok(generated);
    }
 // NEW: PDF / DOC / DOCX -> structured syllabus (Upload File feature)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping(value = "/superadmin/syllabus/extract", consumes = "multipart/form-data")
    public ResponseEntity<?> extractSyllabusFromFile(
            @RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "No file was received by the server."));
            }

            String text = fileTextExtractionService.extractText(file);
            if (text == null || text.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Could not extract any text from this file. It may be a scanned/image-only PDF."));
            }

            List<ExtractedWeekDto> weeks = openAIService.generateSyllabusFromExtractedText(text);
            return ResponseEntity.ok(weeks);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "message", "Syllabus extraction failed: " + e.getMessage()));
        }
    }
}