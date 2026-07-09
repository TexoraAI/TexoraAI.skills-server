package com.lms.course.controller;

import com.lms.course.dto.BannerStudioAiGenerateRequestDTO;
import com.lms.course.dto.BannerStudioAiGenerateResponseDTO;
import com.lms.course.dto.BannerStudioRequestDTO;
import com.lms.course.dto.BannerStudioResponseDTO;
import com.lms.course.dto.BannerStudioStatusUpdateDTO;
import com.lms.course.service.BannerStudioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for the Banner Studio feature, matching the contract
 * expected by the frontend's bannerApi.js (base path: /api/banners).
 */
@RestController
@RequestMapping("/api/banners")
public class BannerStudioController {

    private final BannerStudioService bannerStudioService;

    public BannerStudioController(BannerStudioService bannerStudioService) {
        this.bannerStudioService = bannerStudioService;
    }

    // GET /api/banners?status=all&search=
    @GetMapping
    public ResponseEntity<List<BannerStudioResponseDTO>> getAllBanners(
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "") String search) {
        return ResponseEntity.ok(bannerStudioService.getAllBanners(status, search));
    }

    // GET /api/banners/{id}
    @GetMapping("/{id}")
    public ResponseEntity<BannerStudioResponseDTO> getBannerById(@PathVariable Long id) {
        return ResponseEntity.ok(bannerStudioService.getBannerById(id));
    }

    // POST /api/banners
    @PostMapping
    public ResponseEntity<BannerStudioResponseDTO> createBanner(@RequestBody BannerStudioRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bannerStudioService.createBanner(request));
    }

    // PUT /api/banners/{id}
    @PutMapping("/{id}")
    public ResponseEntity<BannerStudioResponseDTO> updateBanner(
            @PathVariable Long id,
            @RequestBody BannerStudioRequestDTO request) {
        return ResponseEntity.ok(bannerStudioService.updateBanner(id, request));
    }

    // DELETE /api/banners/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBanner(@PathVariable Long id) {
        bannerStudioService.deleteBanner(id);
        return ResponseEntity.noContent().build();
    }

    // POST /api/banners/{id}/duplicate
    @PostMapping("/{id}/duplicate")
    public ResponseEntity<BannerStudioResponseDTO> duplicateBanner(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bannerStudioService.duplicateBanner(id));
    }

    // PATCH /api/banners/{id}/publish
    @PatchMapping("/{id}/publish")
    public ResponseEntity<BannerStudioResponseDTO> publishNow(@PathVariable Long id) {
        return ResponseEntity.ok(bannerStudioService.publishNow(id));
    }

    // PATCH /api/banners/{id}/schedule
    @PatchMapping("/{id}/schedule")
    public ResponseEntity<BannerStudioResponseDTO> schedule(
            @PathVariable Long id,
            @RequestBody BannerStudioStatusUpdateDTO request) {
        return ResponseEntity.ok(bannerStudioService.schedule(id, request));
    }

    // PATCH /api/banners/{id}/status
    @PatchMapping("/{id}/status")
    public ResponseEntity<BannerStudioResponseDTO> updateStatus(
            @PathVariable Long id,
            @RequestBody BannerStudioStatusUpdateDTO request) {
        return ResponseEntity.ok(bannerStudioService.updateStatus(id, request));
    }

    // PATCH /api/banners/{id}/view  (analytics: impression tracking)
    @PatchMapping("/{id}/view")
    public ResponseEntity<BannerStudioResponseDTO> registerView(@PathVariable Long id) {
        return ResponseEntity.ok(bannerStudioService.registerView(id));
    }

    // PATCH /api/banners/{id}/click  (analytics: click tracking)
    @PatchMapping("/{id}/click")
    public ResponseEntity<BannerStudioResponseDTO> registerClick(@PathVariable Long id) {
        return ResponseEntity.ok(bannerStudioService.registerClick(id));
    }

    // POST /api/banners/ai-generate
    @PostMapping("/ai-generate")
    public ResponseEntity<BannerStudioAiGenerateResponseDTO> generateWithAi(
            @RequestBody BannerStudioAiGenerateRequestDTO request) {
        return ResponseEntity.ok(bannerStudioService.generateWithAi(request));
    }

    // POST /api/banners/ai-generate/save
    @PostMapping("/ai-generate/save")
    public ResponseEntity<BannerStudioResponseDTO> saveAiGeneratedBanner(
            @RequestBody BannerStudioAiGenerateResponseDTO aiResult) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bannerStudioService.saveAiGeneratedBanner(aiResult));
    }
}