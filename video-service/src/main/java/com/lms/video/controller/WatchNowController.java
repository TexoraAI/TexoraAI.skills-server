package com.lms.video.controller;

import com.lms.video.dto.WatchNowDTO;
import com.lms.video.model.WatchNow;
import com.lms.video.service.WatchNowService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import com.lms.video.dto.WatchNowStatsDTO;
/**
 * WatchNow REST Controller
 *
 * Base path : /api/v1/watch-now
 *
 * PUBLIC endpoints (no auth required – handled in Gateway + SecurityConfig):
 *   GET  /api/v1/watch-now/all           – list all published entries
 *   GET  /api/v1/watch-now/{id}          – get single entry
 *   GET  /api/v1/watch-now/stream/{file} – stream video / thumbnail file
 *
 * SUPER_ADMIN only:
 *   POST   /api/v1/watch-now/upload      – create new entry
 *   PUT    /api/v1/watch-now/{id}        – update entry
 *   DELETE /api/v1/watch-now/{id}        – delete by primary key
 *   DELETE /api/v1/watch-now/by-course/{courseId} – delete by courseId
 */
@RestController
@RequestMapping("/api/v1/watch-now")
public class WatchNowController {

    private final WatchNowService service;

    public WatchNowController(WatchNowService service) {
        this.service = service;
    }

    // ═══════════════════════════════════════════════════════════════
    //  PUBLIC – list all
    // ═══════════════════════════════════════════════════════════════
    @GetMapping("/all")
    public ResponseEntity<List<WatchNow>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }
    
    @GetMapping("/stats")
    public ResponseEntity<WatchNowStatsDTO> getDashboardStats() {
        return ResponseEntity.ok(service.getDashboardStats());
    }

    // ═══════════════════════════════════════════════════════════════
    //  PUBLIC – get single
    // ═══════════════════════════════════════════════════════════════
    @GetMapping("/{id}")
    public ResponseEntity<WatchNow> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ═══════════════════════════════════════════════════════════════
    //  PUBLIC – stream video or thumbnail file
    // ═══════════════════════════════════════════════════════════════
    @GetMapping("/stream/{fileName:.+}")
    public ResponseEntity<Resource> stream(@PathVariable String fileName) {
        File file = new File(WatchNowService.getVideoDir() + fileName);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        // Pick content type based on extension
        String lower = fileName.toLowerCase();
        String contentType = "video/mp4";
        if (lower.endsWith(".webm"))          contentType = "video/webm";
        else if (lower.endsWith(".mov"))       contentType = "video/quicktime";
        else if (lower.endsWith(".jpg")
              || lower.endsWith(".jpeg"))      contentType = "image/jpeg";
        else if (lower.endsWith(".png"))       contentType = "image/png";
        else if (lower.endsWith(".webp"))      contentType = "image/webp";

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header("Accept-Ranges", "bytes")
                .body(resource);
    }

    // ═══════════════════════════════════════════════════════════════
    //  SUPER_ADMIN – create
    // ═══════════════════════════════════════════════════════════════
    @PostMapping("/upload")
    public ResponseEntity<WatchNow> upload(
            @RequestPart(value = "video", required = false) MultipartFile video,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @ModelAttribute WatchNowDTO dto
    ) throws IOException {
        WatchNow created = service.upload(video, thumbnail, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ═══════════════════════════════════════════════════════════════
    //  SUPER_ADMIN – update
    // ═══════════════════════════════════════════════════════════════
    @PutMapping("/{id}")
    public ResponseEntity<WatchNow> update(
            @PathVariable Long id,
            @RequestPart(value = "video", required = false) MultipartFile video,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @ModelAttribute WatchNowDTO dto
    ) throws IOException {
        WatchNow updated = service.update(id, video, thumbnail, dto);
        return ResponseEntity.ok(updated);
    }

    // ═══════════════════════════════════════════════════════════════
    //  SUPER_ADMIN – delete by primary key
    // ═══════════════════════════════════════════════════════════════
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.ok("WatchNow entry deleted: " + id);
    }

    // ═══════════════════════════════════════════════════════════════
    //  SUPER_ADMIN – delete by courseId (bulk)
    // ═══════════════════════════════════════════════════════════════
    @DeleteMapping("/by-course/{courseId}")
    public ResponseEntity<String> deleteByCourse(@PathVariable Long courseId) {
        service.deleteByCourseId(courseId);
        return ResponseEntity.ok("WatchNow entries deleted for courseId: " + courseId);
    }
   
}