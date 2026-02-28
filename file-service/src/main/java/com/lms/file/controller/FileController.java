//package com.lms.file.controller;
//
//import com.lms.file.model.FileResource;
//import com.lms.file.service.FileService;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.http.*;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//@RestController
//@RequestMapping("/api/files")
//public class FileController {
//
//    private final FileService service;
//
//    public FileController(FileService service) {
//        this.service = service;
//    }
//
//    // ================= UPLOAD =================
//    @PostMapping("/upload")
//    public FileResource upload(
//            @RequestParam("file") MultipartFile file,
//            @RequestHeader("X-ROLE") String role
//    ) throws Exception {
//        return service.upload(file, role);
//    }
//
//    // ================= LIST =================
//    @GetMapping
//    public Page<FileResource> list(Pageable pageable) {
//        return service.listFiles(pageable);
//    }
//
//    // ================= DOWNLOAD =================
//    @GetMapping("/download/{name}")
//    public ResponseEntity<byte[]> download(@PathVariable String name) throws Exception {
//
//        byte[] data = service.download(name);
//
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION,
//                        "attachment; filename=\"" + name + "\"")
//                .body(data);
//    }
//
//    // ================= DELETE =================
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> delete(
//            @PathVariable Long id,
//            @RequestHeader("X-ROLE") String role
//    ) throws Exception {
//
//        service.delete(id, role);
//        return ResponseEntity.noContent().build();
//    }
//}





package com.lms.file.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.lms.file.model.FileResource;
import com.lms.file.service.FileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/file")
public class FileController {

    private final FileService service;

    public FileController(FileService service) {
        this.service = service;
    }

    // ================= TRAINER UPLOAD =================
    @PostMapping("/upload")
    public FileResource upload(
            @RequestParam MultipartFile file,
            @RequestParam Long batchId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description
    ) throws Exception {
        return service.upload(file, batchId,title, description);
    }

    // ================= TRAINER FILES =================
    @GetMapping("/trainer")
    public List<FileResource> trainerFiles() {
        return service.getTrainerFiles();
    }

    // ================= STUDENT FILES =================
    @GetMapping("/student")
    public List<FileResource> studentFiles() {
        return service.getStudentFiles();
    }

    // ================= DOWNLOAD =================
    @GetMapping("/download/{name}")
    public ResponseEntity<byte[]> download(@PathVariable String name) throws Exception {

        byte[] data = service.download(name);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + name + "\"")
                .body(data);
    }
    @GetMapping("/view/{id}")
    public ResponseEntity<byte[]> view(@PathVariable Long id) throws Exception {

        FileResource file = service.getById(id);
        byte[] data = service.viewFile(id);

        String name = file.getOriginalName().toLowerCase();

        String contentType = "application/octet-stream";

        if (name.endsWith(".pdf"))
            contentType = "application/pdf";
        else if (name.endsWith(".png"))
            contentType = "image/png";
        else if (name.endsWith(".jpg") || name.endsWith(".jpeg"))
            contentType = "image/jpeg";
        else if (name.endsWith(".txt"))
            contentType = "text/plain";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + file.getOriginalName() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(data);
    }

    // ================= DELETE (TRAINER ONLY OWN FILE) =================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) throws Exception {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
