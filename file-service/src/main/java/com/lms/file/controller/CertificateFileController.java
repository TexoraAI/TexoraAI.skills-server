//package com.lms.file.controller;
//
//import com.lms.file.service.CertificateFileService;
//import org.springframework.web.bind.annotation.*;
//import com.lms.file.dto.CertificateResponse;
//import java.util.List;
//@RestController
//@RequestMapping("/api/files/certificates")
//public class CertificateFileController {
//
//    private final CertificateFileService service;
//
//    public CertificateFileController(CertificateFileService service) {
//        this.service = service;
//    }
//
//    @PostMapping("/generate")
//    public String generateCertificate(
//            @RequestParam String email,
//            @RequestParam String studentName,
//            @RequestParam String courseName,
//            @RequestParam String type) {
//
//        return service.generateCertificate(
//                email, studentName, courseName, type
//        );
//    }
//    @GetMapping("/student")
//    public List<CertificateResponse> getStudentCertificates(
//            @RequestParam String email) {
//
//        return service.getCertificatesByStudent(email);
//    }
//}






package com.lms.file.controller;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;


import com.lms.file.dto.CertificateResponse;
import com.lms.file.service.CertificateFileService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/files/certificates")
public class CertificateFileController {

    private final CertificateFileService service;

    public CertificateFileController(CertificateFileService service) {
        this.service = service;
    }

    // ================= ISSUE / GENERATE =================
    @PostMapping("/generate")
    public String generateCertificate(
            @RequestParam String email,
            @RequestParam String studentName,
            @RequestParam String courseName,
            @RequestParam String type) {

        return service.generateCertificate(
                email, studentName, courseName, type
        );
    }

    // ================= STUDENT LIST =================
    @GetMapping("/student")
    public List<CertificateResponse> getStudentCertificates(
            @RequestParam String email) {

        return service.getCertificatesByStudent(email);
    }

    // ================= PREVIEW / DOWNLOAD =================
    
 // ================= PREVIEW / DOWNLOAD =================
    @GetMapping("/{fileName}")
    public ResponseEntity<Resource> previewOrDownloadCertificate(
            @PathVariable("fileName") String fileName) {

        Resource resource = service.loadCertificate(fileName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "inline; filename=\"" + fileName + "\""
                )
                .body(resource);
    }


}
