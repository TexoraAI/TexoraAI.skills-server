//package com.lms.assessment.controller;
//
//import com.lms.assessment.dto.CodeExecutionRequest;
//import com.lms.assessment.dto.CodeExecutionResponse;
//import com.lms.assessment.service.CodeExecutionService;
//import com.lms.assessment.service.CodeSubmissionService;
//import jakarta.validation.Valid;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/v1/code")
//public class CodeController {
//
//    private final CodeSubmissionService codeSubmissionService;
//    private final CodeExecutionService codeExecutionService;
//
//    public CodeController(CodeSubmissionService codeSubmissionService,CodeExecutionService codeExecutionService) {
//        this.codeSubmissionService = codeSubmissionService;
//        this.codeExecutionService =codeExecutionService;
//    }
//
//    @PostMapping("/run")
//    @PreAuthorize("hasAnyRole('STUDENT','TRAINER','ADMIN')")
//    public ResponseEntity<CodeExecutionResponse> runCode(
//            @Valid @RequestBody CodeExecutionRequest request) {
////        return ResponseEntity.ok(codeSubmissionService.executeAndSubmit(request));
//    	CodeExecutionService.ExecutionResult result = codeExecutionService.execute(
//                request.getLanguage(),
//                request.getCode(),
//                request.getSampleInput()
//            );
//
//            CodeExecutionResponse response = CodeExecutionResponse.builder()
//                .language(request.getLanguage().toUpperCase())
//                .output(result.getOutput())
//                .status(result.getStatus())
//                .executionTimeMs(result.getElapsedMs())
//                .batchId(request.getBatchId())
//                .build();
//
//            return ResponseEntity.ok(response);
//        }
//    
//
//    @GetMapping("/submissions/student")
//    @PreAuthorize("hasAnyRole('STUDENT','TRAINER','ADMIN')")
//    public ResponseEntity<List<CodeExecutionResponse>> getMySubmissions(
//            @RequestParam String batchId) {
//        return ResponseEntity.ok(codeSubmissionService.getMySubmissions(batchId));
//    }
//
//    @GetMapping("/submissions/batch/{batchId}")
//    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
//    public ResponseEntity<List<CodeExecutionResponse>> getBatchSubmissions(
//            @PathVariable String batchId) {
//        return ResponseEntity.ok(codeSubmissionService.getBatchSubmissions(batchId));
//    }
//    
//    
//} working of 3 pythn java js 

package com.lms.assessment.controller;

import com.lms.assessment.dto.CodeExecutionRequest;
import com.lms.assessment.dto.CodeExecutionResponse;
import com.lms.assessment.service.CodeExecutionService;
import com.lms.assessment.service.CodeFileService;
import com.lms.assessment.service.CodeSubmissionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/code")
public class CodeController {

    private final CodeSubmissionService codeSubmissionService;
    private final CodeExecutionService  codeExecutionService;
    private final CodeFileService codeFileService;

    public CodeController(CodeSubmissionService codeSubmissionService,
                          CodeExecutionService codeExecutionService,
                          CodeFileService codeFileService) {
        this.codeSubmissionService = codeSubmissionService;
        this.codeExecutionService  = codeExecutionService;
        this.codeFileService       = codeFileService;
    }

    // ── Helper: extract student email from JWT ────────────────
    private String getCurrentStudentId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new RuntimeException("Not authenticated");
        }
        return auth.getPrincipal().toString();
    }

    // ── Run Code ──────────────────────────────────────────────
    @PostMapping("/run")
    @PreAuthorize("hasAnyRole('STUDENT','TRAINER','ADMIN')")
    public ResponseEntity<CodeExecutionResponse> runCode(
            @Valid @RequestBody CodeExecutionRequest request) {

        // MySQL gets studentId so it uses persistent DB
        // all other languages get normal sampleInput
        String input = "MYSQL".equalsIgnoreCase(request.getLanguage())
            ? getCurrentStudentId()
            : request.getSampleInput();

        CodeExecutionService.ExecutionResult result = codeExecutionService.execute(
            request.getLanguage(),
            request.getCode(),
            input
        );

        CodeExecutionResponse response = CodeExecutionResponse.builder()
            .language(request.getLanguage().toUpperCase())
            .output(result.getOutput())
            .status(result.getStatus())
            .executionTimeMs(result.getElapsedMs())
            .batchId(request.getBatchId())
            .build();

        return ResponseEntity.ok(response);
    }

    // ── MySQL: get student's current DB state ─────────────────
    @GetMapping("/mysql/state")
    @PreAuthorize("hasAnyRole('STUDENT','TRAINER','ADMIN')")
    public ResponseEntity<Map<String, Object>> getMySQLState() {
        String studentId = getCurrentStudentId();
        CodeExecutionService.ExecutionResult result =
            codeExecutionService.getMySQLDatabaseState(studentId);
        return ResponseEntity.ok(Map.of(
            "output",          result.getOutput() != null ? result.getOutput() : "",
            "status",          result.getStatus().name(),
            "executionTimeMs", result.getElapsedMs()
        ));
    }

    // ── MySQL: reset (drop + recreate) student's DB ───────────
    @DeleteMapping("/mysql/reset")
    @PreAuthorize("hasAnyRole('STUDENT','TRAINER','ADMIN')")
    public ResponseEntity<Map<String, Object>> resetMySQL() {
        String studentId = getCurrentStudentId();
        CodeExecutionService.ExecutionResult result =
            codeExecutionService.resetMySQLDatabase(studentId);
        return ResponseEntity.ok(Map.of(
            "output",          result.getOutput() != null ? result.getOutput() : "",
            "status",          result.getStatus().name(),
            "executionTimeMs", result.getElapsedMs()
        ));
    }

    // ── Submissions ───────────────────────────────────────────
    @GetMapping("/submissions/student")
    @PreAuthorize("hasAnyRole('STUDENT','TRAINER','ADMIN')")
    public ResponseEntity<List<CodeExecutionResponse>> getMySubmissions(
            @RequestParam String batchId) {
        return ResponseEntity.ok(codeSubmissionService.getMySubmissions(batchId));
    }

    @GetMapping("/submissions/batch/{batchId}")
    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
    public ResponseEntity<List<CodeExecutionResponse>> getBatchSubmissions(
            @PathVariable String batchId) {
        return ResponseEntity.ok(codeSubmissionService.getBatchSubmissions(batchId));
    }
    
    
    
}