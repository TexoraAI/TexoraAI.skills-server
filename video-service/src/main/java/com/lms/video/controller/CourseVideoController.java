
package com.lms.video.controller;
import java.util.Map;
import com.lms.video.dto.TranscriptResponse;
import com.lms.video.model.FeaturedTranscriptSegment;
import com.lms.video.model.TranscriptSourceType;
import com.lms.video.model.TranscriptStatus;
import com.lms.video.repository.FeaturedVideoTranscriptRepository;
import com.lms.video.repository.FeaturedTranscriptSegmentRepository;
import com.lms.video.security.JwtUtil;
import java.util.List;
import com.lms.video.model.CourseVideo;
import com.lms.video.service.CourseVideoService;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URI;
import com.lms.video.repository.CourseVideoRepository;
@RestController
@RequestMapping("/api/course-videos")
public class CourseVideoController {

    private final CourseVideoService service;
    private final FeaturedVideoTranscriptRepository transcriptRepo;
    private final FeaturedTranscriptSegmentRepository segmentRepo;
    private final CourseVideoRepository repo;
    private final JwtUtil jwtUtil; // NEW

    public CourseVideoController(CourseVideoService service,
            CourseVideoRepository repo,
            FeaturedVideoTranscriptRepository transcriptRepo,
            FeaturedTranscriptSegmentRepository segmentRepo,
            JwtUtil jwtUtil) { // NEW param
        this.service = service;
        this.repo = repo;
        this.transcriptRepo = transcriptRepo;
        this.segmentRepo = segmentRepo;
        this.jwtUtil = jwtUtil; // NEW
    }

    // NEW — same helper pattern as VideoController's orgIdFrom
    private String orgIdFrom(String authHeader) {
        if (authHeader == null) return null;
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        return jwtUtil.extractOrganizationId(token);
    }

    // ================= UPLOAD =================
    @PostMapping("/upload")
    public CourseVideo upload(
            @RequestHeader(value = "Authorization", required = false) String authHeader, // NEW
            @RequestParam MultipartFile file,
            @RequestParam Long courseId,
            @RequestParam Long moduleId,
            @RequestParam Long batchId,
            Authentication auth
    ) throws IOException {
        String organizationId = orgIdFrom(authHeader); // NEW
        return service.upload(file, courseId, moduleId, batchId, auth.getName(), organizationId);
    }

    // ================= EDIT =================
    // Accepts multipart/form-data so a replacement file is optional.
    // Frontend sends: file (optional), courseId, moduleId, batchId
    @PutMapping("/{id}")
    public CourseVideo update(
            @RequestHeader(value = "Authorization", required = false) String authHeader, // NEW
            @PathVariable Long id,
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long moduleId,
            @RequestParam(required = false) Long batchId,
            Authentication auth
    ) throws IOException {
        String organizationId = orgIdFrom(authHeader); // NEW
        return service.update(
                id,
                file,
                courseId,
                moduleId,
                batchId,
                auth != null ? auth.getName() : null,
                organizationId
        );
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.ok("Course video deleted successfully");
    }

    // ================= PLAYBACK URL (JSON) =================
    @GetMapping("/play/{fileName:.+}")
    public ResponseEntity<Map<String, String>> getPlaybackUrlJson(
            @PathVariable String fileName,
            Authentication auth
    ) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String presignedUrl = service.getPlaybackUrl(fileName);
        return ResponseEntity.ok(Map.of("url", presignedUrl));
    }
    // ================= TRANSCRIPT =================
    @GetMapping("/{id}/transcript")
    public TranscriptResponse getTranscript(@PathVariable Long id) {
        return transcriptRepo.findBySessionIdAndSourceType(id, TranscriptSourceType.COURSE_VIDEO)
                .map(transcript -> {
                    if (transcript.getStatus() == TranscriptStatus.READY) {
                        List<FeaturedTranscriptSegment> segments =
                                segmentRepo.findByTranscriptIdOrderByOrderIndexAsc(transcript.getId());
                        List<TranscriptResponse.SegmentDto> segmentDtos = segments.stream()
                                .map(s -> new TranscriptResponse.SegmentDto(s.getStartSeconds(), s.getEndSeconds(), s.getText()))
                                .toList();
                        return new TranscriptResponse("READY", transcript.getLanguage(), null, segmentDtos);
                    }
                    if (transcript.getStatus() == TranscriptStatus.FAILED) {
                        return new TranscriptResponse("FAILED", transcript.getLanguage(), transcript.getErrorMessage(), List.of());
                    }
                    return new TranscriptResponse(transcript.getStatus().name(), transcript.getLanguage(), null, List.of());
                })
                .orElse(new TranscriptResponse("NONE", null, null, List.of()));
    }
    // ================= TRANSCRIPT BY URL =================
    @GetMapping("/transcript-by-url")
    public TranscriptResponse getTranscriptByUrl(@RequestParam String url) {
        return repo.findByUrl(url)
                .flatMap(video -> transcriptRepo.findBySessionIdAndSourceType(video.getId(), TranscriptSourceType.COURSE_VIDEO))
                .map(transcript -> {
                    if (transcript.getStatus() == TranscriptStatus.READY) {
                        List<FeaturedTranscriptSegment> segments =
                                segmentRepo.findByTranscriptIdOrderByOrderIndexAsc(transcript.getId());
                        List<TranscriptResponse.SegmentDto> segmentDtos = segments.stream()
                                .map(s -> new TranscriptResponse.SegmentDto(s.getStartSeconds(), s.getEndSeconds(), s.getText()))
                                .toList();
                        return new TranscriptResponse("READY", transcript.getLanguage(), null, segmentDtos);
                    }
                    if (transcript.getStatus() == TranscriptStatus.FAILED) {
                        return new TranscriptResponse("FAILED", transcript.getLanguage(), transcript.getErrorMessage(), List.of());
                    }
                    return new TranscriptResponse(transcript.getStatus().name(), transcript.getLanguage(), null, List.of());
                })
                .orElse(new TranscriptResponse("NONE", null, null, List.of()));
    }
    
    // ================= USAGE (video course-content storage quota) =================
    @GetMapping("/upload-quota")
    public java.util.Map<String, Object> getUploadQuota(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            Authentication auth
    ) {
        String organizationId = orgIdFrom(authHeader);
        return service.getCourseVideoUsage(auth.getName(), organizationId);
    }
}