//
//package com.lms.assessment.controller;
//
//import com.lms.assessment.dto.CodeExecutionRequest;
//import com.lms.assessment.dto.CodeExecutionResponse;
//import com.lms.assessment.service.CodeExecutionService;
//import com.lms.assessment.service.CodeFileService;
//import com.lms.assessment.service.CodeSubmissionService;
//import jakarta.validation.Valid;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/v1/code")
//public class CodeController {
//
//    private final CodeSubmissionService codeSubmissionService;
//    private final CodeExecutionService  codeExecutionService;
//    private final CodeFileService codeFileService;
//
//    public CodeController(CodeSubmissionService codeSubmissionService,
//                          CodeExecutionService codeExecutionService,
//                          CodeFileService codeFileService) {
//        this.codeSubmissionService = codeSubmissionService;
//        this.codeExecutionService  = codeExecutionService;
//        this.codeFileService       = codeFileService;
//    }
//
//    // ── Helper: extract student email from JWT ────────────────
//    private String getCurrentStudentId() {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        if (auth == null || auth.getPrincipal() == null) {
//            throw new RuntimeException("Not authenticated");
//        }
//        return auth.getPrincipal().toString();
//    }
//
//    // ── Run Code ──────────────────────────────────────────────
//    @PostMapping("/run")
//    @PreAuthorize("hasAnyRole('STUDENT','TRAINER','ADMIN')")
//    public ResponseEntity<CodeExecutionResponse> runCode(
//            @Valid @RequestBody CodeExecutionRequest request) {
//
//        // MySQL gets studentId so it uses persistent DB
//        // all other languages get normal sampleInput
//        String input = "MYSQL".equalsIgnoreCase(request.getLanguage())
//            ? getCurrentStudentId()
//            : request.getSampleInput();
//
//        CodeExecutionService.ExecutionResult result = codeExecutionService.execute(
//            request.getLanguage(),
//            request.getCode(),
//            input
//        );
//
//        CodeExecutionResponse response = CodeExecutionResponse.builder()
//            .language(request.getLanguage().toUpperCase())
//            .output(result.getOutput())
//            .status(result.getStatus())
//            .executionTimeMs(result.getElapsedMs())
//            .batchId(request.getBatchId())
//            .build();
//
//        return ResponseEntity.ok(response);
//    }
//
//    // ── MySQL: get student's current DB state ─────────────────
//    @GetMapping("/mysql/state")
//    @PreAuthorize("hasAnyRole('STUDENT','TRAINER','ADMIN')")
//    public ResponseEntity<Map<String, Object>> getMySQLState() {
//        String studentId = getCurrentStudentId();
//        CodeExecutionService.ExecutionResult result =
//            codeExecutionService.getMySQLDatabaseState(studentId);
//        return ResponseEntity.ok(Map.of(
//            "output",          result.getOutput() != null ? result.getOutput() : "",
//            "status",          result.getStatus().name(),
//            "executionTimeMs", result.getElapsedMs()
//        ));
//    }
//
//    // ── MySQL: reset (drop + recreate) student's DB ───────────
//    @DeleteMapping("/mysql/reset")
//    @PreAuthorize("hasAnyRole('STUDENT','TRAINER','ADMIN')")
//    public ResponseEntity<Map<String, Object>> resetMySQL() {
//        String studentId = getCurrentStudentId();
//        CodeExecutionService.ExecutionResult result =
//            codeExecutionService.resetMySQLDatabase(studentId);
//        return ResponseEntity.ok(Map.of(
//            "output",          result.getOutput() != null ? result.getOutput() : "",
//            "status",          result.getStatus().name(),
//            "executionTimeMs", result.getElapsedMs()
//        ));
//    }
//
//    // ── Submissions ───────────────────────────────────────────
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
//}

package com.lms.assessment.controller;

import com.lms.assessment.constants.AssessmentFeatureKeys;
import com.lms.assessment.dto.CodeExecutionRequest;
import com.lms.assessment.dto.CodeExecutionResponse;
import com.lms.assessment.service.AssessmentFeatureFlagsService;
import com.lms.assessment.service.CodeExecutionService;
import com.lms.assessment.service.CodeFileService;
import com.lms.assessment.service.CodeSubmissionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
    private final AssessmentFeatureFlagsService featureFlagsService;

    public CodeController(CodeSubmissionService codeSubmissionService,
                          CodeExecutionService codeExecutionService,
                          CodeFileService codeFileService,
                          AssessmentFeatureFlagsService featureFlagsService) {
        this.codeSubmissionService = codeSubmissionService;
        this.codeExecutionService  = codeExecutionService;
        this.codeFileService       = codeFileService;
        this.featureFlagsService   = featureFlagsService;
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
            @Valid @RequestBody CodeExecutionRequest request,
            HttpServletRequest httpRequest) {

        // Only gate when the caller is a STUDENT — trainers/admins testing their
        // own code against this endpoint must never be blocked by the student flag.
        if (isStudentCaller()) {
            featureFlagsService.enforce(orgId(httpRequest), callerEmail(), AssessmentFeatureKeys.SOLVE_CODING_PROBLEM);
        }

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
    public ResponseEntity<Map<String, Object>> getMySQLState(HttpServletRequest httpRequest) {
        featureFlagsService.enforce(orgId(httpRequest), callerEmail(), AssessmentFeatureKeys.SOLVE_CODING_PROBLEM);
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
    public ResponseEntity<Map<String, Object>> resetMySQL(HttpServletRequest httpRequest) {
        featureFlagsService.enforce(orgId(httpRequest), callerEmail(), AssessmentFeatureKeys.SOLVE_CODING_PROBLEM);
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
            @RequestParam String batchId,
            HttpServletRequest httpRequest) {
        featureFlagsService.enforce(orgId(httpRequest), callerEmail(), AssessmentFeatureKeys.SOLVE_CODING_PROBLEM);
        return ResponseEntity.ok(codeSubmissionService.getMySubmissions(batchId));
    }

    @GetMapping("/submissions/batch/{batchId}")
    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
    public ResponseEntity<List<CodeExecutionResponse>> getBatchSubmissions(
            @PathVariable String batchId,
            HttpServletRequest httpRequest) {
        // Only gate when the caller is TRAINER — ADMIN viewing batch submissions
        // as an org report must never be blocked by the trainer-facing flag.
        if (!isAdminCaller()) {
            featureFlagsService.enforce(orgId(httpRequest), callerEmail(), AssessmentFeatureKeys.CREATE_CODING_PROBLEM);
        }
        return ResponseEntity.ok(codeSubmissionService.getBatchSubmissions(batchId));
    }

    // 🏢 Pulled from the JwtFilter's request attribute — never from the request body.
    private String orgId(HttpServletRequest request) {
        return (String) request.getAttribute("organizationId");
    }

    // ── Feature-flag role helpers ─────────────────────────────────────────────
    private String callerEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    private boolean isAdminCaller() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        for (GrantedAuthority ga : auth.getAuthorities()) {
            String r = ga.getAuthority();
            if (r.equals("ROLE_ADMIN") || r.equals("ROLE_TENANT_ADMIN") || r.equals("ROLE_SUPER_ADMIN")) {
                return true;
            }
        }
        return false;
    }

    private boolean isStudentCaller() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        for (GrantedAuthority ga : auth.getAuthorities()) {
            if (ga.getAuthority().equals("ROLE_STUDENT")) {
                return true;
            }
        }
        return false;
    }
}