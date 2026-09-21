//package com.lms.video.controller;
//
//import com.lms.video.model.FeaturedSessionVideo;
//import com.lms.video.service.FeaturedSessionVideoService;
//import org.springframework.core.io.*;
//import org.springframework.http.*;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.*;
//
//@RestController
//@RequestMapping("/api/video/v1/featured/session")
//public class FeaturedSessionVideoController {
//
//    private final FeaturedSessionVideoService service;
//
//    private static final String VIDEO_DIR =
//            System.getProperty("user.dir") + "/videos/featured-content/";
//
//    public FeaturedSessionVideoController(FeaturedSessionVideoService service) {
//        this.service = service;
//    }
//
//    // ================= UPLOAD =================
//    @PreAuthorize("hasRole('SUPER_ADMIN')")
//    @PostMapping
//    public FeaturedSessionVideo upload(
//            @RequestParam MultipartFile file,
//            @RequestParam Long sessionId,
//            @RequestParam(required = false) String title,
//            @RequestParam(required = false) String description,
//            @RequestParam(required = false) MultipartFile thumbnail
//    ) throws IOException {
//        return service.upload(file, sessionId, title, description, thumbnail);
//    }
//
//    // ================= GET BY SESSION =================
//    // Used by ManualSyllabusBuilder / ProgramPlayer to fetch title, description,
//    // thumbnail, duration, status for a session without waiting on the polling loop.
//    @GetMapping("/session/{sessionId}")
//    public FeaturedSessionVideo getBySession(@PathVariable Long sessionId) {
//        return service.getBySession(sessionId);
//    }
//
//    // ================= EDIT METADATA / REPLACE VIDEO / REPLACE THUMBNAIL =================
//    // All params optional so the admin can edit just the title, just swap the
//    // thumbnail, just replace the video file, or any combination in one call.
//    @PreAuthorize("hasRole('SUPER_ADMIN')")
//    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public FeaturedSessionVideo update(
//            @PathVariable Long id,
//            @RequestParam(required = false) String title,
//            @RequestParam(required = false) String description,
//            @RequestParam(required = false) MultipartFile thumbnail,
//            @RequestParam(required = false) MultipartFile newVideo
//    ) throws IOException {
//        return service.update(id, title, description, thumbnail, newVideo);
//    }
//
//    // ================= DELETE =================
//    @PreAuthorize("hasRole('SUPER_ADMIN')")
//    @DeleteMapping("/{id}")
//    public void delete(@PathVariable Long id) {
//        service.delete(id);
//    }
//
//    // ================= STREAM ================= (unchanged)
//    @GetMapping("/stream/{fileName:.+}")
//    public ResponseEntity<Resource> streamVideo(
//            @PathVariable String fileName,
//            @RequestHeader HttpHeaders headers
//    ) throws IOException {
//
//        File file = new File(VIDEO_DIR + fileName);
//        if (!file.exists()) {
//            return ResponseEntity.notFound().build();
//        }
//
//        long fileLength = file.length();
//        String rangeHeader = headers.getFirst(HttpHeaders.RANGE);
//
//        if (rangeHeader == null) {
//            Resource resource = new FileSystemResource(file);
//            return ResponseEntity.ok()
//                    .contentType(MediaTypeFactory.getMediaType(fileName)
//                            .orElse(MediaType.APPLICATION_OCTET_STREAM))
//                    .contentLength(fileLength)
//                    .body(resource);
//        }
//
//        String[] ranges = rangeHeader.replace("bytes=", "").split("-");
//        long start = Long.parseLong(ranges[0]);
//        long end = ranges.length > 1 && !ranges[1].isEmpty()
//                ? Long.parseLong(ranges[1])
//                : fileLength - 1;
//        long contentLength = end - start + 1;
//
//        HttpHeaders responseHeaders = new HttpHeaders();
//        responseHeaders.add("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);
//        responseHeaders.add("Accept-Ranges", "bytes");
//
//        InputStream inputStream = new FileInputStream(file);
//        inputStream.skip(start);
//
//        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
//                .headers(responseHeaders)
//                .contentLength(contentLength)
//                .contentType(MediaTypeFactory.getMediaType(fileName)
//                        .orElse(MediaType.APPLICATION_OCTET_STREAM))
//                .body(new InputStreamResource(inputStream));
//    }
//}





package com.lms.video.controller;

import com.lms.video.model.FeaturedSessionVideo;
import com.lms.video.service.FeaturedSessionVideoService;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;

@RestController
@RequestMapping("/api/video/v1/featured/session")
public class FeaturedSessionVideoController {

    private final FeaturedSessionVideoService service;

    public FeaturedSessionVideoController(FeaturedSessionVideoService service) {
        this.service = service;
    }

    // ================= UPLOAD =================
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping
    public FeaturedSessionVideo upload(
            @RequestParam MultipartFile file,
            @RequestParam Long sessionId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) MultipartFile thumbnail
    ) throws IOException {
        return service.upload(file, sessionId, title, description, thumbnail);
    }

    // ================= GET BY SESSION =================
    // Used by ManualSyllabusBuilder / ProgramPlayer to fetch title, description,
    // thumbnail, duration, status for a session without waiting on the polling loop.
    @GetMapping("/session/{sessionId}")
    public FeaturedSessionVideo getBySession(@PathVariable Long sessionId) {
        return service.getBySession(sessionId);
    }

    // ================= EDIT METADATA / REPLACE VIDEO / REPLACE THUMBNAIL =================
    // All params optional so the admin can edit just the title, just swap the
    // thumbnail, just replace the video file, or any combination in one call.
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FeaturedSessionVideo update(
            @PathVariable Long id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) MultipartFile thumbnail,
            @RequestParam(required = false) MultipartFile newVideo
    ) throws IOException {
        return service.update(id, title, description, thumbnail, newVideo);
    }

    // ================= DELETE =================
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    // ================= STREAM =================
    // ✅ CHANGED — instead of reading local file bytes and manually parsing
    // Range headers, redirect the browser straight to a temporary S3 link.
    // Works for both the video file and the thumbnail image — S3 natively
    // supports Range requests, so seeking still works exactly the same.
    @GetMapping("/stream/{fileName:.+}")
    public ResponseEntity<Void> streamVideo(@PathVariable String fileName) {
        String presignedUrl = service.getPlaybackUrl(fileName);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(presignedUrl))
                .build();
    }
}