package com.lms.user.controller;

import com.lms.user.dto.AIResumeRequestDTO;
import com.lms.user.dto.AIResumeResponseDTO;
import com.lms.user.dto.LinkedInScrapeRequestDTO;
import com.lms.user.dto.ResumeRequestDTO;
import com.lms.user.dto.ResumeResponseDTO;
import com.lms.user.service.AIResumeService;
import com.lms.user.service.LinkedInScraperService;
import com.lms.user.service.ResumeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/resume")
public class ResumeController {

    private static final Logger log = LoggerFactory.getLogger(ResumeController.class);

    private final ResumeService resumeService;
    private final AIResumeService aiResumeService;
    private final LinkedInScraperService linkedInScraperService;

    public ResumeController(ResumeService resumeService,
                            AIResumeService aiResumeService,
                            LinkedInScraperService linkedInScraperService) {
        this.resumeService = resumeService;
        this.aiResumeService = aiResumeService;
        this.linkedInScraperService = linkedInScraperService;
    }

    // =========================================================================
    // CRUD
    // =========================================================================

    // CREATE
    @PostMapping("/{userId}")
    public ResponseEntity<ResumeResponseDTO> createResume(
            @PathVariable Long userId,
            @Valid @RequestBody ResumeRequestDTO request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resumeService.createResume(userId, request));
    }

    // GET ALL
    @GetMapping("/{userId}")
    public ResponseEntity<List<ResumeResponseDTO>> getAllResumes(
            @PathVariable Long userId) {

        return ResponseEntity.ok(resumeService.getAllResumes(userId));
    }

    // GET ONE
    @GetMapping("/{userId}/{resumeId}")
    public ResponseEntity<ResumeResponseDTO> getResume(
            @PathVariable Long userId,
            @PathVariable Long resumeId) {

        return ResponseEntity.ok(resumeService.getResumeById(userId, resumeId));
    }

    // UPDATE
    @PutMapping("/{userId}/{resumeId}")
    public ResponseEntity<ResumeResponseDTO> updateResume(
            @PathVariable Long userId,
            @PathVariable Long resumeId,
            @Valid @RequestBody ResumeRequestDTO request) {

        return ResponseEntity.ok(resumeService.updateResume(userId, resumeId, request));
    }

    // DELETE
    @DeleteMapping("/{userId}/{resumeId}")
    public ResponseEntity<Void> deleteResume(
            @PathVariable Long userId,
            @PathVariable Long resumeId) {

        resumeService.deleteResume(userId, resumeId);
        return ResponseEntity.noContent().build();
    }

    // DUPLICATE
    @PostMapping("/{userId}/{resumeId}/duplicate")
    public ResponseEntity<ResumeResponseDTO> duplicateResume(
            @PathVariable Long userId,
            @PathVariable Long resumeId) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resumeService.duplicateResume(userId, resumeId));
    }

    // =========================================================================
    // AI ENDPOINTS
    // =========================================================================

    // AI Generate resume (with optional LinkedIn scrape internally)
    @PostMapping("/{userId}/ai/generate")
    public ResponseEntity<ResumeRequestDTO> generateResume(
            @PathVariable Long userId,
            @RequestBody AIResumeRequestDTO.GenerateRequest request) {

        log.info("AI generate resume — userId={}, jobTitle={}", userId, request.getJobTitle());
        return ResponseEntity.ok(aiResumeService.generateResume(request));
    }

    // AI Parse PDF
    @PostMapping("/{userId}/ai/parse-pdf")
    public ResponseEntity<ResumeRequestDTO> parsePdf(
            @PathVariable Long userId,
            @RequestBody AIResumeRequestDTO.ParsePdfRequest request) {

        log.info("PDF parse — userId={}, file={}", userId, request.getFileName());
        return ResponseEntity.ok(aiResumeService.parsePdf(request));
    }

    // AI ATS Tips
    @PostMapping("/{userId}/ai/ats-tips")
    public ResponseEntity<AIResumeResponseDTO.AtsTipsResponse> getAtsTips(
            @PathVariable Long userId,
            @RequestBody AIResumeRequestDTO.AtsTipsRequest request) {

        log.info("ATS tips — userId={}", userId);
        return ResponseEntity.ok(aiResumeService.getAtsTips(request));
    }

 // AI Writing Assistant
    @PostMapping("/{userId}/ai/write")
    public ResponseEntity<Map<String, String>> aiWrite(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {
        log.info("AI write — userId={}, section={}", userId, request.get("section"));
        String result = aiResumeService.rewriteSection(request.get("section"), request.get("input"));
        return ResponseEntity.ok(Map.of("text", result));
    }
    
    
    
    // =========================================================================
    // LINKEDIN SCRAPER ENDPOINTS  (merged from LinkedInScraperController)
    // Previously at: /api/v1/resume/linkedin/*
    // Now at:        /api/v1/resume/{userId}/linkedin/*
    // =========================================================================

    /**
     * POST /api/v1/resume/{userId}/linkedin/scrape
     *
     * Request body:
     * {
     *   "linkedInUrl":  "https://linkedin.com/in/yourprofile",  ← required
     *   "jobTitle":     "Senior Java Developer",                ← optional
     *   "extraSkills":  "Docker, Kubernetes",                   ← optional
     *   "templateName": "classic"                               ← optional
     * }
     *
     * Response: complete ResumeRequestDTO ready to save/display
     */
//    @PostMapping("/{userId}/linkedin/scrape")
//    public ResponseEntity<?> scrapeAndBuildResume(
//            @PathVariable Long userId,
//            @RequestBody LinkedInScrapeRequestDTO request) {
//
//        log.info("LinkedIn scrape — userId={}, url={}", userId, request.getLinkedInUrl());
//
//        if (request.getLinkedInUrl() == null || request.getLinkedInUrl().isBlank()) {
//            return ResponseEntity.badRequest()
//                    .body(Map.of("error", "linkedInUrl is required"));
//        }
//
//        if (!request.getLinkedInUrl().contains("linkedin.com/in/")) {
//            return ResponseEntity.badRequest()
//                    .body(Map.of("error",
//                            "Please provide a valid LinkedIn profile URL. " +
//                            "Example: https://linkedin.com/in/yourname"));
//        }
//
//        try {
//            ResumeRequestDTO resume = linkedInScraperService.buildResumeFromLinkedIn(request);
//            log.info("Resume built successfully from LinkedIn scrape — userId={}", userId);
//            return ResponseEntity.ok(resume);
//
//        } catch (Exception e) {
//            log.error("LinkedIn scrape failed for userId={}: {}", userId, e.getMessage());
//            return ResponseEntity.internalServerError()
//                    .body(Map.of(
//                            "error",   "Failed to build resume from LinkedIn",
//                            "message", e.getMessage(),
//                            "tip",     "Make sure your LinkedIn profile is set to PUBLIC"
//                    ));
//        }
//    }
    @PostMapping("/{userId}/linkedin/scrape")
    public ResponseEntity<?> scrapeAndBuildResume(
            @PathVariable Long userId,
            @RequestBody LinkedInScrapeRequestDTO request) {

        log.info("LinkedIn scrape — userId={}, url={}, hasPdf={}", 
                 userId, request.getLinkedInUrl(), 
                 request.getBase64Pdf() != null && !request.getBase64Pdf().isEmpty());

        boolean hasPdf = request.getBase64Pdf() != null && !request.getBase64Pdf().isBlank();
        boolean hasUrl = request.getLinkedInUrl() != null && !request.getLinkedInUrl().isBlank();

        // Must have either a PDF or a URL
        if (!hasPdf && !hasUrl) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Either a LinkedIn PDF or linkedInUrl is required"));
        }

        if (hasUrl && !request.getLinkedInUrl().contains("linkedin.com/in/")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Please provide a valid LinkedIn profile URL."));
        }

        try {
            ResumeRequestDTO resume = linkedInScraperService.buildResumeFromLinkedIn(request);
            log.info("Resume built successfully — userId={}", userId);
            return ResponseEntity.ok(resume);
        } catch (Exception e) {
            log.error("LinkedIn scrape failed for userId={}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "error",   "Failed to build resume from LinkedIn",
                            "message", e.getMessage(),
                            "tip",     "Make sure the PDF is a valid LinkedIn profile export"
                    ));
        }
    }
    /**
     * GET /api/v1/resume/{userId}/linkedin/validate?url=...
     *
     * Quick check — is the URL a valid LinkedIn profile URL?
     * Frontend can call this on blur to show instant feedback.
     */
    @GetMapping("/{userId}/linkedin/validate")
    public ResponseEntity<Map<String, Object>> validateLinkedInUrl(
            @PathVariable Long userId,
            @RequestParam String url) {

        boolean valid = url != null
                && url.contains("linkedin.com/in/")
                && url.length() > 25;

        return ResponseEntity.ok(Map.of(
                "valid",   valid,
                "message", valid
                        ? "Valid LinkedIn URL"
                        : "Please enter a valid LinkedIn profile URL"
        ));
    }
}