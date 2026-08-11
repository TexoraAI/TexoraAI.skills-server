

package com.lms.video.controller;
import com.lms.video.dto.TranscriptResponse;
import com.lms.video.model.FeaturedTranscriptSegment;
import com.lms.video.model.TranscriptSourceType;
import com.lms.video.model.TranscriptStatus;
import com.lms.video.repository.FeaturedVideoTranscriptRepository;
import com.lms.video.repository.FeaturedTranscriptSegmentRepository;
import java.util.List;
import com.lms.video.model.CourseVideo;
import com.lms.video.service.CourseVideoService;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import com.lms.video.repository.CourseVideoRepository;
@RestController
@RequestMapping("/api/course-videos")
public class CourseVideoController {

    private final CourseVideoService service;
    private final FeaturedVideoTranscriptRepository transcriptRepo;
    private final FeaturedTranscriptSegmentRepository segmentRepo;
    private final CourseVideoRepository repo;
   
    private static final String VIDEO_DIR =
            System.getProperty("user.dir") + "/videos/course-content/";

    public CourseVideoController(CourseVideoService service,
            CourseVideoRepository repo,
            FeaturedVideoTranscriptRepository transcriptRepo,
            FeaturedTranscriptSegmentRepository segmentRepo) {
        this.service = service;
        this.repo = repo;
        this.transcriptRepo = transcriptRepo;
        this.segmentRepo = segmentRepo;
    }

    // ================= UPLOAD =================
    @PostMapping("/upload")
    public CourseVideo upload(
            @RequestParam MultipartFile file,
            @RequestParam Long courseId,
            @RequestParam Long moduleId,
            @RequestParam Long batchId,
            Authentication auth
    ) throws IOException {
        return service.upload(file, courseId, moduleId, batchId, auth.getName());
    }

    // ================= EDIT =================
    // Accepts multipart/form-data so a replacement file is optional.
    // Frontend sends: file (optional), courseId, moduleId, batchId
    @PutMapping("/{id}")
    public CourseVideo update(
            @PathVariable Long id,
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long moduleId,
            @RequestParam(required = false) Long batchId,
            Authentication auth
    ) throws IOException {
        return service.update(
                id,
                file,
                courseId,
                moduleId,
                batchId,
                auth != null ? auth.getName() : null
        );
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.ok("Course video deleted successfully");
    }

    // ================= STREAM =================
    @GetMapping("/stream/{fileName:.+}")
    public ResponseEntity<Resource> streamVideo(
            @PathVariable String fileName,
            @RequestHeader HttpHeaders headers,
            Authentication auth
    ) throws IOException {

        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        File file = new File(VIDEO_DIR + fileName);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        long fileLength = file.length();
        String rangeHeader = headers.getFirst(HttpHeaders.RANGE);

        if (rangeHeader == null) {
            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .contentType(MediaTypeFactory.getMediaType(fileName)
                            .orElse(MediaType.APPLICATION_OCTET_STREAM))
                    .contentLength(fileLength)
                    .body(resource);
        }

        String[] ranges = rangeHeader.replace("bytes=", "").split("-");
        long start = Long.parseLong(ranges[0]);
        long end = ranges.length > 1 && !ranges[1].isEmpty()
                ? Long.parseLong(ranges[1])
                : fileLength - 1;
        long contentLength = end - start + 1;

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);
        responseHeaders.add("Accept-Ranges", "bytes");

        InputStream inputStream = new FileInputStream(file);
        inputStream.skip(start);

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .headers(responseHeaders)
                .contentLength(contentLength)
                .contentType(MediaTypeFactory.getMediaType(fileName)
                        .orElse(MediaType.APPLICATION_OCTET_STREAM))
                .body(new InputStreamResource(inputStream));
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
    // ContentItem (course-service) only stores the video's url, not
    // video-service's internal CourseVideo.id — same reason
    // ContentEventConsumer resolves by url instead of id. This endpoint
    // lets the frontend do the same lookup.
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
}