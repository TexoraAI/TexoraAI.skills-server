//package com.lms.assessment.controller;
//
//import com.lms.assessment.dto.CodeFileDTO;
//import com.lms.assessment.service.CodeFileService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/v1/code-files")   // ← base path
//public class CodeFileController {
//
//    @Autowired
//    private CodeFileService codeFileService;
//
//    // GET /api/v1/code-files/profile
//    // Frontend calls this once on load to get student email from JWT
//    @GetMapping("/profile")
//    public ResponseEntity<?> getProfile() {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        String email = auth.getName();
//        return ResponseEntity.ok(Map.of("email", email));
//    }
//
//    // POST /api/v1/code-files          ← assessmentService: saveCodeFile(data)
//    @PostMapping
//    public ResponseEntity<CodeFileDTO> save(
//            @RequestBody CodeFileDTO.SaveRequest req) {
//        return ResponseEntity.ok(codeFileService.save(req));
//    }
//
//    // GET /api/v1/code-files/my?batchId=...  ← assessmentService: getMyCodeFiles(batchId)
//    // JWT carries student identity — no studentEmail param needed
//    @GetMapping("/my")
//    public ResponseEntity<List<CodeFileDTO>> getMy(
//            @RequestParam String batchId) {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        String studentEmail = auth.getName();
//        return ResponseEntity.ok(codeFileService.getAll(studentEmail, batchId));
//    }
//
//    // GET /api/v1/code-files/{id}      ← assessmentService: getCodeFileById(id)
//    @GetMapping("/{id}")
//    public ResponseEntity<CodeFileDTO> getById(@PathVariable Long id) {
//        return ResponseEntity.ok(codeFileService.getById(id));
//    }
//
//    // PUT /api/v1/code-files/{id}      ← assessmentService: updateCodeFile(id, data)
//    @PutMapping("/{id}")
//    public ResponseEntity<CodeFileDTO> update(
//            @PathVariable Long id,
//            @RequestBody CodeFileDTO.UpdateRequest req) {
//        return ResponseEntity.ok(codeFileService.update(id, req));
//    }
//
//    // DELETE /api/v1/code-files/{id}   ← assessmentService: deleteCodeFile(id)
//    @DeleteMapping("/{id}")
//    public ResponseEntity<?> delete(@PathVariable Long id) {
//        codeFileService.delete(id);
//        return ResponseEntity.ok(Map.of("message", "File deleted successfully"));
//    }
//}
package com.lms.assessment.controller;

import com.lms.assessment.dto.CodeFileDTO;
import com.lms.assessment.service.CodeFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/code-files")   // ← base path
public class CodeFileController {

    @Autowired
    private CodeFileService codeFileService;

    // GET /api/v1/code-files/profile
    // Frontend calls this once on load to get student email from JWT
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return ResponseEntity.ok(Map.of("email", email));
    }

    // POST /api/v1/code-files          ← assessmentService: saveCodeFile(data)
    // studentEmail is taken from JWT — frontend does NOT need to send it
    @PostMapping
    public ResponseEntity<CodeFileDTO> save(
            @RequestBody CodeFileDTO.SaveRequest req) {

        // ✅ Always override studentEmail from JWT — never trust frontend value
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        req.setStudentEmail(auth.getName());

        return ResponseEntity.ok(codeFileService.save(req));
    }

    // GET /api/v1/code-files/my?batchId=...  ← assessmentService: getMyCodeFiles(batchId)
    // JWT carries student identity — no studentEmail param needed
    @GetMapping("/my")
    public ResponseEntity<List<CodeFileDTO>> getMy(
            @RequestParam String batchId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String studentEmail = auth.getName();
        return ResponseEntity.ok(codeFileService.getAll(studentEmail, batchId));
    }

    // GET /api/v1/code-files/{id}      ← assessmentService: getCodeFileById(id)
    @GetMapping("/{id}")
    public ResponseEntity<CodeFileDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(codeFileService.getById(id));
    }

    // PUT /api/v1/code-files/{id}      ← assessmentService: updateCodeFile(id, data)
    @PutMapping("/{id}")
    public ResponseEntity<CodeFileDTO> update(
            @PathVariable Long id,
            @RequestBody CodeFileDTO.UpdateRequest req) {
        return ResponseEntity.ok(codeFileService.update(id, req));
    }

    // DELETE /api/v1/code-files/{id}   ← assessmentService: deleteCodeFile(id)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        codeFileService.delete(id);
        return ResponseEntity.ok(Map.of("message", "File deleted successfully"));
    }
}