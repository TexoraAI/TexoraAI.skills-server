package com.lms.file.controller;

import com.lms.file.model.FeaturedSessionFile;
import com.lms.file.service.FeaturedSessionFileService;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@RestController
@RequestMapping("/api/featured-files")
public class FeaturedSessionFileController {

    private final FeaturedSessionFileService service;

    private static final String FILE_DIR =
            System.getProperty("user.dir") + "/files/featured-content/";

    public FeaturedSessionFileController(FeaturedSessionFileService service) {
        this.service = service;
    }

    // ================= UPLOAD (direct from frontend) =================
    @PostMapping("/upload")
    public FeaturedSessionFile upload(
            @RequestParam MultipartFile file,
            @RequestParam Long sessionId
    ) {
        return service.upload(file, sessionId);
    }

    // ================= SECURED DOWNLOAD (same guard style as CourseFileController) =================
    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> download(@PathVariable String fileName) {
        File file = new File(FILE_DIR + fileName);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(file);
        MediaType contentType = resolveContentType(fileName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .contentType(contentType)
                .contentLength(file.length())
                .body(resource);
    }

    private MediaType resolveContentType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".pdf")) return MediaType.APPLICATION_PDF;
        if (lower.endsWith(".doc")) return MediaType.valueOf("application/msword");
        if (lower.endsWith(".docx")) {
            return MediaType.valueOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}