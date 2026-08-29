package com.lms.progress.controller;

import com.lms.progress.dto.RoadmapUpgradedAdminStatsDto;
import com.lms.progress.dto.RoadmapUpgradedGenerateRequestDto;
import com.lms.progress.dto.RoadmapUpgradedMentorMessageDto;
import com.lms.progress.dto.RoadmapUpgradedMentorRequestDto;
import com.lms.progress.dto.RoadmapUpgradedMentorResponseDto;
import com.lms.progress.dto.RoadmapUpgradedResponseDto;
import com.lms.progress.dto.RoadmapUpgradedSuperAdminStatsDto;
import com.lms.progress.service.RoadmapUpgradedService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Single controller class for the RoadmapUpgraded feature, per spec. Every
 * endpoint reads the Authorization header, does zero business logic itself,
 * and delegates straight to RoadmapUpgradedService. All role checks (403s)
 * happen inside the service, not here.
 */
@RestController
@RequestMapping("/api/roadmap-upgraded")
public class RoadmapUpgradedController {

    private final RoadmapUpgradedService service;

    public RoadmapUpgradedController(RoadmapUpgradedService service) {
        this.service = service;
    }

//    @PostMapping("/generate")
//    public RoadmapUpgradedResponseDto generate(@RequestHeader("Authorization") String authHeader,
//                                                @RequestBody RoadmapUpgradedGenerateRequestDto request) {
//        return service.generateRoadmap(extractToken(authHeader), request);
//    }
    @PostMapping("/generate")
    public ResponseEntity<RoadmapUpgradedResponseDto> generate(@RequestHeader("Authorization") String authHeader,
                                                @RequestBody RoadmapUpgradedGenerateRequestDto request) {
        RoadmapUpgradedResponseDto result = service.startRoadmapGeneration(extractToken(authHeader), request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(result);
    }

    @GetMapping("/my")
    public List<RoadmapUpgradedResponseDto> getMyRoadmaps(@RequestHeader("Authorization") String authHeader) {
        return service.getMyRoadmaps(extractToken(authHeader));
    }

    @GetMapping("/{id}")
    public RoadmapUpgradedResponseDto getRoadmapById(@RequestHeader("Authorization") String authHeader,
                                                       @PathVariable Long id) {
        return service.getRoadmapById(extractToken(authHeader), id);
    }

    @PostMapping("/resource/{id}/complete")
    public RoadmapUpgradedResponseDto markResourceComplete(@RequestHeader("Authorization") String authHeader,
                                                             @PathVariable Long id,
                                                             @RequestParam(required = false) Integer quizScore) {
        return service.markResourceComplete(extractToken(authHeader), id, quizScore);
    }

    /**
     * Streams the real generated PDF for a PDF-type resource back as
     * application/pdf, so the frontend can render it inline (iframe/object
     * URL) instead of redirecting. Same auth pattern as every other
     * endpoint here - access control (owner / same-org admin / super admin)
     * happens inside the service, not here. See
     * RoadmapUpgradedService.getResourcePdf() and
     * RoadmapUpgradedService.generatePdfResource().
     */
    @GetMapping("/resource/{id}/pdf")
    public ResponseEntity<byte[]> getResourcePdf(@RequestHeader("Authorization") String authHeader,
                                                  @PathVariable Long id) {
        byte[] pdf = service.getResourcePdf(extractToken(authHeader), id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PostMapping("/{id}/regenerate")
    public RoadmapUpgradedResponseDto regenerateRemainingModules(@RequestHeader("Authorization") String authHeader,
                                                                   @PathVariable Long id) {
        return service.regenerateRemainingModules(extractToken(authHeader), id);
    }

    @PostMapping("/{id}/clone")
    public RoadmapUpgradedResponseDto cloneAsTemplate(@RequestHeader("Authorization") String authHeader,
                                                        @PathVariable Long id) {
        return service.cloneAsTemplate(extractToken(authHeader), id);
    }

    @GetMapping("/admin/stats")
    public RoadmapUpgradedAdminStatsDto getAdminStats(@RequestHeader("Authorization") String authHeader) {
        return service.getAdminStats(extractToken(authHeader));
    }

    @GetMapping("/super-admin/stats")
    public RoadmapUpgradedSuperAdminStatsDto getSuperAdminStats(@RequestHeader("Authorization") String authHeader) {
        return service.getSuperAdminStats(extractToken(authHeader));
    }

    @PostMapping("/mentor/ask")
    public RoadmapUpgradedMentorResponseDto askMentor(@RequestHeader("Authorization") String authHeader,
                                                        @RequestBody RoadmapUpgradedMentorRequestDto request) {
        return service.askMentor(extractToken(authHeader), request);
    }

    @GetMapping("/mentor/{syllabusId}/history")
    public List<RoadmapUpgradedMentorMessageDto> getMentorHistory(@RequestHeader("Authorization") String authHeader,
                                                                    @PathVariable Long syllabusId) {
        return service.getMentorHistory(extractToken(authHeader), syllabusId);
    }

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or malformed Authorization header");
        }
        return authHeader.substring("Bearer ".length());
    }
    
    /**
     * Streams a single PDF of the ENTIRE roadmap (all modules/topics/
     * resources) - separate from GET /resource/{id}/pdf above, which only
     * ever covers one resource's own generated PDF. See
     * RoadmapUpgradedService.exportRoadmapPdf().
     */
    @GetMapping("/{id}/export-pdf")
    public ResponseEntity<byte[]> exportRoadmapPdf(@RequestHeader("Authorization") String authHeader,
                                                    @PathVariable Long id) {
        byte[] pdf = service.exportRoadmapPdf(extractToken(authHeader), id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header("Content-Disposition", "attachment; filename=\"roadmap.pdf\"")
                .body(pdf);
    }
}