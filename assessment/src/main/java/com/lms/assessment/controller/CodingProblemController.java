//
//
//package com.lms.assessment.controller;
//
//import com.lms.assessment.dto.CodingProblemRequest;
//import com.lms.assessment.dto.CodingProblemResponse;
//import com.lms.assessment.dto.TestCaseRequest;
//import com.lms.assessment.dto.TestCaseResponse;
//import com.lms.assessment.service.CodingProblemService;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.validation.Valid;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//import com.lms.assessment.dto.CodingProblemAdminReportResponse;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/v1/problems")
//public class CodingProblemController {
//
//    private final CodingProblemService codingProblemService;
//
//    public CodingProblemController(CodingProblemService codingProblemService) {
//        this.codingProblemService = codingProblemService;
//    }
//
//    // POST /api/v1/problems
//    @PostMapping
//    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
//    public ResponseEntity<CodingProblemResponse> createProblem(
//            @Valid @RequestBody CodingProblemRequest request,
//            HttpServletRequest httpRequest) {
//        return ResponseEntity.status(HttpStatus.CREATED)
//            .body(codingProblemService.createProblem(request, orgId(httpRequest)));
//    }
//
//    // PUT /api/v1/problems/{problemId}
//    @PutMapping("/{problemId}")
//    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
//    public ResponseEntity<CodingProblemResponse> updateProblem(
//            @PathVariable Long problemId,
//            @Valid @RequestBody CodingProblemRequest request,
//            HttpServletRequest httpRequest) {
//        return ResponseEntity.ok(codingProblemService.updateProblem(problemId, request, orgId(httpRequest)));
//    }
//
//    // DELETE /api/v1/problems/{problemId}
//    @DeleteMapping("/{problemId}")
//    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
//    public ResponseEntity<Void> deleteProblem(@PathVariable Long problemId, HttpServletRequest httpRequest) {
//        codingProblemService.deleteProblem(problemId, orgId(httpRequest));
//        return ResponseEntity.noContent().build();
//    }
//
//    // GET /api/v1/problems/my  (trainer sees own problems)
//    @GetMapping("/my")
//    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
//    public ResponseEntity<List<CodingProblemResponse>> getMyProblems(HttpServletRequest httpRequest) {
//        return ResponseEntity.ok(codingProblemService.getMyProblems(orgId(httpRequest)));
//    }
//
//    // GET /api/v1/problems/{problemId}  (trainer full view)
//    @GetMapping("/{problemId}")
//    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
//    public ResponseEntity<CodingProblemResponse> getProblemById(
//            @PathVariable Long problemId,
//            HttpServletRequest httpRequest) {
//        return ResponseEntity.ok(codingProblemService.getProblemById(problemId, orgId(httpRequest)));
//    }
//
//    // POST /api/v1/problems/{problemId}/testcases
//    @PostMapping("/{problemId}/testcases")
//    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
//    public ResponseEntity<TestCaseResponse> addTestCase(
//            @PathVariable Long problemId,
//            @Valid @RequestBody TestCaseRequest request,
//            HttpServletRequest httpRequest) {
//        return ResponseEntity.status(HttpStatus.CREATED)
//            .body(codingProblemService.addTestCase(problemId, request, orgId(httpRequest)));
//    }
//
//    // GET /api/v1/problems/{problemId}/testcases
//    @GetMapping("/{problemId}/testcases")
//    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
//    public ResponseEntity<List<TestCaseResponse>> getTestCases(
//            @PathVariable Long problemId,
//            HttpServletRequest httpRequest) {
//        return ResponseEntity.ok(codingProblemService.getTestCases(problemId, orgId(httpRequest)));
//    }
//
//    // 🏢 Pulled from the JwtFilter's request attribute — never from the request body.
//    private String orgId(HttpServletRequest request) {
//        return (String) request.getAttribute("organizationId");
//    }
//    @GetMapping("/admin")
//    @PreAuthorize("hasRole('TENANT_ADMIN')")
//    public ResponseEntity<List<CodingProblemAdminReportResponse>> getAdminReport(HttpServletRequest httpRequest) {
//        return ResponseEntity.ok(codingProblemService.getAdminReport(orgId(httpRequest)));
//    }
//
//    @GetMapping("/superadmin")
//    @PreAuthorize("hasRole('SUPER_ADMIN')")
//    public ResponseEntity<List<CodingProblemAdminReportResponse>> getSuperAdminReport() {
//        return ResponseEntity.ok(codingProblemService.getSuperAdminReport());
//    } 
//}


package com.lms.assessment.controller;

import com.lms.assessment.constants.AssessmentFeatureKeys;
import com.lms.assessment.dto.CodingProblemRequest;
import com.lms.assessment.dto.CodingProblemResponse;
import com.lms.assessment.dto.TestCaseRequest;
import com.lms.assessment.dto.TestCaseResponse;
import com.lms.assessment.service.AssessmentFeatureFlagsService;
import com.lms.assessment.service.CodingProblemService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.lms.assessment.dto.CodingProblemAdminReportResponse;
import java.util.List;

@RestController
@RequestMapping("/api/v1/problems")
public class CodingProblemController {

    private final CodingProblemService codingProblemService;
    private final AssessmentFeatureFlagsService featureFlagsService;

    public CodingProblemController(CodingProblemService codingProblemService,
                                   AssessmentFeatureFlagsService featureFlagsService) {
        this.codingProblemService = codingProblemService;
        this.featureFlagsService = featureFlagsService;
    }

    // POST /api/v1/problems
    @PostMapping
    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
    public ResponseEntity<CodingProblemResponse> createProblem(
            @Valid @RequestBody CodingProblemRequest request,
            HttpServletRequest httpRequest) {
        // Only gate when the caller is TRAINER — ADMIN is never gated.
        if (!isAdminCaller()) {
            featureFlagsService.enforce(orgId(httpRequest), callerEmail(), AssessmentFeatureKeys.CREATE_CODING_PROBLEM);
        }
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(codingProblemService.createProblem(request, orgId(httpRequest)));
    }

    // PUT /api/v1/problems/{problemId}
    @PutMapping("/{problemId}")
    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
    public ResponseEntity<CodingProblemResponse> updateProblem(
            @PathVariable Long problemId,
            @Valid @RequestBody CodingProblemRequest request,
            HttpServletRequest httpRequest) {
        if (!isAdminCaller()) {
            featureFlagsService.enforce(orgId(httpRequest), callerEmail(), AssessmentFeatureKeys.CREATE_CODING_PROBLEM);
        }
        return ResponseEntity.ok(codingProblemService.updateProblem(problemId, request, orgId(httpRequest)));
    }

    // DELETE /api/v1/problems/{problemId}
    @DeleteMapping("/{problemId}")
    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
    public ResponseEntity<Void> deleteProblem(@PathVariable Long problemId, HttpServletRequest httpRequest) {
        if (!isAdminCaller()) {
            featureFlagsService.enforce(orgId(httpRequest), callerEmail(), AssessmentFeatureKeys.CREATE_CODING_PROBLEM);
        }
        codingProblemService.deleteProblem(problemId, orgId(httpRequest));
        return ResponseEntity.noContent().build();
    }

    // GET /api/v1/problems/my  (trainer sees own problems)
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
    public ResponseEntity<List<CodingProblemResponse>> getMyProblems(HttpServletRequest httpRequest) {
        if (!isAdminCaller()) {
            featureFlagsService.enforce(orgId(httpRequest), callerEmail(), AssessmentFeatureKeys.CREATE_CODING_PROBLEM);
        }
        return ResponseEntity.ok(codingProblemService.getMyProblems(orgId(httpRequest)));
    }

    // GET /api/v1/problems/{problemId}  (trainer full view)
    @GetMapping("/{problemId}")
    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
    public ResponseEntity<CodingProblemResponse> getProblemById(
            @PathVariable Long problemId,
            HttpServletRequest httpRequest) {
        if (!isAdminCaller()) {
            featureFlagsService.enforce(orgId(httpRequest), callerEmail(), AssessmentFeatureKeys.CREATE_CODING_PROBLEM);
        }
        return ResponseEntity.ok(codingProblemService.getProblemById(problemId, orgId(httpRequest)));
    }

    // POST /api/v1/problems/{problemId}/testcases
    @PostMapping("/{problemId}/testcases")
    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
    public ResponseEntity<TestCaseResponse> addTestCase(
            @PathVariable Long problemId,
            @Valid @RequestBody TestCaseRequest request,
            HttpServletRequest httpRequest) {
        if (!isAdminCaller()) {
            featureFlagsService.enforce(orgId(httpRequest), callerEmail(), AssessmentFeatureKeys.CREATE_CODING_PROBLEM);
        }
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(codingProblemService.addTestCase(problemId, request, orgId(httpRequest)));
    }

    // GET /api/v1/problems/{problemId}/testcases
    @GetMapping("/{problemId}/testcases")
    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
    public ResponseEntity<List<TestCaseResponse>> getTestCases(
            @PathVariable Long problemId,
            HttpServletRequest httpRequest) {
        if (!isAdminCaller()) {
            featureFlagsService.enforce(orgId(httpRequest), callerEmail(), AssessmentFeatureKeys.CREATE_CODING_PROBLEM);
        }
        return ResponseEntity.ok(codingProblemService.getTestCases(problemId, orgId(httpRequest)));
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

    @GetMapping("/admin")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<List<CodingProblemAdminReportResponse>> getAdminReport(HttpServletRequest httpRequest) {
        featureFlagsService.enforce(orgId(httpRequest), callerEmail(), AssessmentFeatureKeys.VIEW_CODING_PROBLEM_ADMIN_REPORT);
        return ResponseEntity.ok(codingProblemService.getAdminReport(orgId(httpRequest)));
    }

    @GetMapping("/superadmin")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<CodingProblemAdminReportResponse>> getSuperAdminReport() {
        return ResponseEntity.ok(codingProblemService.getSuperAdminReport());
    } 
}