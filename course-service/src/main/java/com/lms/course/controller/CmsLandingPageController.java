package com.lms.course.controller;

import com.lms.course.dto.CmsComponentDtos;
import com.lms.course.dto.CmsCommonDtos;
import com.lms.course.dto.CmsMediaDtos;
import com.lms.course.dto.CmsNavDtos;
import com.lms.course.dto.CmsPageDtos;
import com.lms.course.dto.CmsSectionDtos;
import com.lms.course.model.CmsMediaAsset;
import com.lms.course.service.CmsLandingPageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST controller powering the Super Admin CMS Management panel
 * (student-hub / trainer-hub / admin-hub, and any future pageKey).
 * Admin endpoints require an authenticated SUPER_ADMIN (enforced at the
 * gateway); the /public/{pageKey} and /media/{id}/raw GETs are open.
 */
@RestController
@RequestMapping("/api/v1/cmslandinghubs")
public class CmsLandingPageController {

    private final CmsLandingPageService cmsService;

    public CmsLandingPageController(CmsLandingPageService cmsService) {
        this.cmsService = cmsService;
    }

    // ────────────────────────────────────────────────────────────────
    // Page
    // ────────────────────────────────────────────────────────────────

    @GetMapping("/{pageKey}")
    public ResponseEntity<CmsPageDtos.Response> getPage(@PathVariable String pageKey) {
        return ResponseEntity.ok(cmsService.getPage(pageKey));
    }

    @GetMapping("/public/{pageKey}")
    public ResponseEntity<CmsPageDtos.Response> getPublicPage(@PathVariable String pageKey) {
        return ResponseEntity.ok(cmsService.getPublishedPage(pageKey));
    }

    @PutMapping("/{pageKey}/settings")
    public ResponseEntity<CmsPageDtos.Response> updatePageSettings(
            @PathVariable String pageKey,
            @Valid @RequestBody CmsPageDtos.SettingsRequest request) {
        return ResponseEntity.ok(cmsService.updatePageSettings(pageKey, request));
    }

    // ────────────────────────────────────────────────────────────────
    // Sections
    // ────────────────────────────────────────────────────────────────

    @PostMapping("/{pageKey}/sections")
    public ResponseEntity<CmsSectionDtos.Response> addSection(
            @PathVariable String pageKey,
            @Valid @RequestBody CmsSectionDtos.CreateRequest request) {
        CmsSectionDtos.Response created = cmsService.addSection(pageKey, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{pageKey}/sections/{sectionId}")
    public ResponseEntity<CmsSectionDtos.Response> updateSection(
            @PathVariable String pageKey,
            @PathVariable Long sectionId,
            @Valid @RequestBody CmsSectionDtos.UpdateRequest request) {
        return ResponseEntity.ok(cmsService.updateSection(pageKey, sectionId, request));
    }

    @DeleteMapping("/{pageKey}/sections/{sectionId}")
    public ResponseEntity<Void> deleteSection(
            @PathVariable String pageKey,
            @PathVariable Long sectionId) {
        cmsService.deleteSection(pageKey, sectionId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{pageKey}/sections/reorder")
    public ResponseEntity<Void> reorderSections(
            @PathVariable String pageKey,
            @Valid @RequestBody CmsSectionDtos.ReorderRequest request) {
        cmsService.reorderSections(pageKey, request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{pageKey}/sections/{sectionId}/visibility")
    public ResponseEntity<Void> setSectionVisibility(
            @PathVariable String pageKey,
            @PathVariable Long sectionId,
            @Valid @RequestBody CmsCommonDtos.VisibilityRequest request) {
        cmsService.setSectionVisibility(pageKey, sectionId, request.getVisible());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{pageKey}/sections/{sectionId}/publish")
    public ResponseEntity<Void> setSectionPublished(
            @PathVariable String pageKey,
            @PathVariable Long sectionId,
            @Valid @RequestBody CmsCommonDtos.PublishRequest request) {
        cmsService.setSectionPublished(pageKey, sectionId, request.getPublished());
        return ResponseEntity.noContent().build();
    }

    // ────────────────────────────────────────────────────────────────
    // Components
    // ────────────────────────────────────────────────────────────────

    @PostMapping("/{pageKey}/sections/{sectionId}/components")
    public ResponseEntity<CmsComponentDtos.Response> addComponent(
            @PathVariable String pageKey,
            @PathVariable Long sectionId,
            @Valid @RequestBody CmsComponentDtos.CreateRequest request) {
        CmsComponentDtos.Response created = cmsService.addComponent(sectionId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{pageKey}/components/{componentId}")
    public ResponseEntity<CmsComponentDtos.Response> updateComponent(
            @PathVariable String pageKey,
            @PathVariable Long componentId,
            @Valid @RequestBody CmsComponentDtos.UpdateRequest request) {
        return ResponseEntity.ok(cmsService.updateComponent(componentId, request));
    }

    @DeleteMapping("/{pageKey}/components/{componentId}")
    public ResponseEntity<Void> deleteComponent(
            @PathVariable String pageKey,
            @PathVariable Long componentId) {
        cmsService.deleteComponent(componentId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{pageKey}/components/{componentId}/visibility")
    public ResponseEntity<Void> setComponentVisibility(
            @PathVariable String pageKey,
            @PathVariable Long componentId,
            @Valid @RequestBody CmsCommonDtos.VisibilityRequest request) {
        cmsService.setComponentVisibility(componentId, request.getVisible());
        return ResponseEntity.noContent().build();
    }

    // ────────────────────────────────────────────────────────────────
    // Media (global, DB-stored bytes)
    // ────────────────────────────────────────────────────────────────

    @GetMapping("/media")
    public ResponseEntity<List<CmsMediaDtos.Response>> listMedia(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(cmsService.listMedia(search));
    }

    @PostMapping(value = "/media/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CmsMediaDtos.Response> uploadMedia(
            @RequestParam("file") MultipartFile file) {
        CmsMediaDtos.Response uploaded = cmsService.uploadMedia(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(uploaded);
    }

    @GetMapping("/media/{mediaId}/raw")
    public ResponseEntity<byte[]> getMediaRaw(@PathVariable Long mediaId) {
        CmsMediaAsset asset = cmsService.getMediaRaw(mediaId);

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(
                    asset.getContentType() != null ? asset.getContentType() : "application/octet-stream");
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + asset.getOriginalFileName() + "\"")
                .body(asset.getFileData());
    }

    @DeleteMapping("/media/{mediaId}")
    public ResponseEntity<Void> deleteMedia(@PathVariable Long mediaId) {
        cmsService.deleteMedia(mediaId);
        return ResponseEntity.noContent().build();
    }

    // ────────────────────────────────────────────────────────────────
    // Navigation (per pageKey)
    // ────────────────────────────────────────────────────────────────

    @GetMapping("/{pageKey}/navigation")
    public ResponseEntity<List<CmsNavDtos.Response>> listNavItems(@PathVariable String pageKey) {
        return ResponseEntity.ok(cmsService.listNavItems(pageKey));
    }

    @PostMapping("/{pageKey}/navigation")
    public ResponseEntity<CmsNavDtos.Response> addNavItem(
            @PathVariable String pageKey,
            @Valid @RequestBody CmsNavDtos.ItemRequest request) {
        CmsNavDtos.Response created = cmsService.addNavItem(pageKey, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{pageKey}/navigation/{itemId}")
    public ResponseEntity<CmsNavDtos.Response> updateNavItem(
            @PathVariable String pageKey,
            @PathVariable Long itemId,
            @Valid @RequestBody CmsNavDtos.ItemRequest request) {
        return ResponseEntity.ok(cmsService.updateNavItem(pageKey, itemId, request));
    }

    @DeleteMapping("/{pageKey}/navigation/{itemId}")
    public ResponseEntity<Void> deleteNavItem(
            @PathVariable String pageKey,
            @PathVariable Long itemId) {
        cmsService.deleteNavItem(pageKey, itemId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{pageKey}/navigation/reorder")
    public ResponseEntity<Void> reorderNavItems(
            @PathVariable String pageKey,
            @Valid @RequestBody CmsNavDtos.ReorderRequest request) {
        cmsService.reorderNavItems(pageKey, request);
        return ResponseEntity.noContent().build();
    }
}