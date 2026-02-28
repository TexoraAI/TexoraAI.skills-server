//package com.lms.file.service;
//
//import com.lms.file.model.CertificateFile;
//import com.lms.file.repository.CertificateFileRepository;
//import com.lms.file.util.CertificatePdfGenerator;
//import org.springframework.stereotype.Service;
//import com.lms.file.dto.CertificateResponse;
//import java.time.LocalDateTime;
//import java.util.List;
//@Service
//public class CertificateFileService {
//
//    private final CertificateFileRepository repository;
//    private final CertificatePdfGenerator pdfGenerator;
//
//    public CertificateFileService(CertificateFileRepository repository,
//                                  CertificatePdfGenerator pdfGenerator) {
//        this.repository = repository;
//        this.pdfGenerator = pdfGenerator;
//    }
//
//    // ================= ISSUE / GENERATE =================
//    public String generateCertificate(String email,
//                                      String studentName,
//                                      String courseName,
//                                      String certificateType) {
//
//        String fileName = pdfGenerator.generate(
//                studentName, courseName, certificateType
//        );
//
//        CertificateFile certificateFile = new CertificateFile();
//        certificateFile.setStudentEmail(email);
//        certificateFile.setStudentName(studentName);
//        certificateFile.setCourseName(courseName);
//        certificateFile.setCertificateType(certificateType);
//        certificateFile.setFileName(fileName);
//        certificateFile.setFilePath("files/certificates/" + fileName);
//        certificateFile.setIssuedAt(LocalDateTime.now()); // ✅ correct
//        
//
//        repository.save(certificateFile);
//
//        return certificateFile.getFilePath();
//    }
//
//    // ================= STUDENT FETCH =================
//    public List<CertificateResponse> getCertificatesByStudent(String email) {
//
//        return repository.findByStudentEmail(email)
//                .stream()
//                .map(c -> new CertificateResponse(
//                        c.getFileName(),
//                        c.getCourseName(),
//                        c.getCertificateType(),       // ✅ fixed
//                        c.getIssuedAt().toString()     // ✅ fixed
//                ))
//                .toList();
//    }
//}





package com.lms.file.service;

import com.lms.file.model.CertificateFile;
import com.lms.file.repository.CertificateFileRepository;
import com.lms.file.util.CertificatePdfGenerator;
import com.lms.file.dto.CertificateResponse;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CertificateFileService {

    private final CertificateFileRepository repository;
    private final CertificatePdfGenerator pdfGenerator;

    public CertificateFileService(CertificateFileRepository repository,
                                  CertificatePdfGenerator pdfGenerator) {
        this.repository = repository;
        this.pdfGenerator = pdfGenerator;
    }

    // ================= ISSUE / GENERATE =================
    public String generateCertificate(String email,
                                      String studentName,
                                      String courseName,
                                      String certificateType) {

        String fileName = pdfGenerator.generate(
                studentName, courseName, certificateType
        );

        CertificateFile certificateFile = new CertificateFile();
        certificateFile.setStudentEmail(email);
        certificateFile.setStudentName(studentName);
        certificateFile.setCourseName(courseName);
        certificateFile.setCertificateType(certificateType);
        certificateFile.setFileName(fileName);
        certificateFile.setFilePath("files/certificates/" + fileName);
        certificateFile.setIssuedAt(LocalDateTime.now());

        repository.save(certificateFile);
        return certificateFile.getFilePath();
    }

    // ================= STUDENT FETCH =================
    public List<CertificateResponse> getCertificatesByStudent(String email) {
        return repository.findByStudentEmail(email)
                .stream()
                .map(c -> new CertificateResponse(
                        c.getFileName(),
                        c.getCourseName(),
                        c.getCertificateType(),
                        c.getIssuedAt() != null
                                ? c.getIssuedAt().toString()
                                : "-"
                ))
                .toList();
    }

    // ================= LOAD CERTIFICATE FILE =================
    public Resource loadCertificate(String fileName) {
        try {
            Path filePath = Paths.get("files/certificates")
                    .resolve(fileName)
                    .normalize();

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                throw new RuntimeException("Certificate not found: " + fileName);
            }

            return resource;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load certificate", e);
        }
    }
}

