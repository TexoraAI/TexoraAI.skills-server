//
//
//
//
//
//
//package com.lms.file.controller;
//
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
//import com.lms.file.model.FileResource;
//import com.lms.file.service.FileService;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/file")
//public class FileController {
//
//    private final FileService service;
//
//    public FileController(FileService service) {
//        this.service = service;
//    }
//
//    // ================= TRAINER UPLOAD =================
//    @PostMapping("/upload")
//    public FileResource upload(
//            @RequestParam MultipartFile file,
//            @RequestParam Long batchId,
//            @RequestParam(required = false) String title,
//            @RequestParam(required = false) String description
//    ) throws Exception {
//        return service.upload(file, batchId,title, description);
//    }
//
//    // ================= TRAINER FILES =================
//    @GetMapping("/trainer")
//    public List<FileResource> trainerFiles() {
//        return service.getTrainerFiles();
//    }
//
//    // ================= STUDENT FILES =================
//    @GetMapping("/student")
//    public List<FileResource> studentFiles() {
//        return service.getStudentFiles();
//    }
//    
//    
//    
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
//    @GetMapping("/view/{id}")
//    public ResponseEntity<byte[]> view(@PathVariable Long id) throws Exception {
//
//        FileResource file = service.getById(id);
//        byte[] data = service.viewFile(id);
//
//        String name = file.getOriginalName().toLowerCase();
//
//        String contentType = "application/octet-stream";
//
//        if (name.endsWith(".pdf"))
//            contentType = "application/pdf";
//        else if (name.endsWith(".png"))
//            contentType = "image/png";
//        else if (name.endsWith(".jpg") || name.endsWith(".jpeg"))
//            contentType = "image/jpeg";
//        else if (name.endsWith(".txt"))
//            contentType = "text/plain";
//
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION,
//                        "inline; filename=\"" + file.getOriginalName() + "\"")
//                .header(HttpHeaders.CONTENT_TYPE, contentType)
//                .body(data);
//    }
//
//    // ================= DELETE (TRAINER ONLY OWN FILE) =================
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> delete(@PathVariable Long id) throws Exception {
//        service.delete(id);
//        return ResponseEntity.noContent().build();
//    }
//}
package com.lms.file.controller;

import com.lms.file.model.FileResource;
import com.lms.file.service.FileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

//    }
 // ================= TRAINER UPLOAD — batchId now optional =================
    @PostMapping("/upload")
    public FileResource upload(
            @RequestParam MultipartFile file,
            @RequestParam(required = false) Long batchId,    // ✅ optional now
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "draft") String status  // ✅ new
    ) throws Exception {
        return service.upload(file, batchId, title, description, courseId, category, status);
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

    
 // ================= VIEW / PREVIEW =================
    @GetMapping("/view/{id}")
    public ResponseEntity<byte[]> view(@PathVariable Long id) throws Exception {
        FileResource file = service.getById(id);
        byte[] data = service.viewFile(id);

        String name = file.getOriginalName() != null
                ? file.getOriginalName().toLowerCase() : "";

        String contentType;

        if (name.endsWith(".pdf"))        contentType = "application/pdf";
        else if (name.endsWith(".png"))   contentType = "image/png";
        else if (name.endsWith(".jpg") || name.endsWith(".jpeg")) contentType = "image/jpeg";
        else if (name.endsWith(".gif"))   contentType = "image/gif";
        else if (name.endsWith(".webp"))  contentType = "image/webp";
        else if (name.endsWith(".txt"))   contentType = "text/plain";
        else if (name.endsWith(".docx"))  contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        else if (name.endsWith(".doc"))   contentType = "application/msword";
        else if (name.endsWith(".pptx"))  contentType = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        else if (name.endsWith(".ppt"))   contentType = "application/vnd.ms-powerpoint";
        else if (name.endsWith(".xlsx"))  contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        else if (name.endsWith(".xls"))   contentType = "application/vnd.ms-excel";
        else if (name.endsWith(".zip"))   contentType = "application/zip";
        else                              contentType = "application/octet-stream";

        // ✅ Always inline — never attachment — no download forced
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + file.getOriginalName() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
                        HttpHeaders.CONTENT_DISPOSITION, HttpHeaders.CONTENT_TYPE)
                .body(data);
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) throws Exception {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
 // ✅ ADD — publish a draft file that already has a batch
    @PatchMapping("/{id}/publish")
    public ResponseEntity<FileResource> publishFile(@PathVariable Long id) {
        return ResponseEntity.ok(service.publishFile(id));
    }

    // ✅ ADD — assign batch to a no-batch draft (also sets published)
    @PatchMapping("/{id}/assign-batch")
    public ResponseEntity<FileResource> assignBatch(
            @PathVariable Long id,
            @RequestParam Long batchId
    ) {
        return ResponseEntity.ok(service.assignBatch(id, batchId));
    }
    ///
    @PutMapping(value = "/{id}/edit", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileResource> editFile(
            @PathVariable Long id,
            @RequestParam(value = "file",        required = false) MultipartFile file,
            @RequestParam(value = "title",        required = false) String title,
            @RequestParam(value = "description",  defaultValue = "") String description,
            @RequestParam(value = "batchId",      required = false) Long batchId,
            @RequestParam(value = "courseId",     required = false) Long courseId,
            @RequestParam(value = "category",     defaultValue = "") String category,
            @RequestParam(value = "status",       defaultValue = "draft") String status
    ) throws Exception {
        FileResource updated = service.editFile(
                id, file, title, description, batchId, courseId, category, status
        );
        return ResponseEntity.ok(updated);
    }
}