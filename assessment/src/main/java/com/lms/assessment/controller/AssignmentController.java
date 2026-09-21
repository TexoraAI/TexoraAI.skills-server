

package com.lms.assessment.controller;

import com.lms.assessment.constants.AssessmentFeatureKeys;
import com.lms.assessment.constants.AssessmentUsageLimits;
import com.lms.assessment.dto.AssignmentResponse;
import com.lms.assessment.dto.CreateAssignmentRequest;
import com.lms.assessment.service.AssessmentFeatureFlagsService;
import com.lms.assessment.service.AssessmentUsageService;
import com.lms.assessment.service.AssignmentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import com.lms.assessment.dto.AssignmentAdminReportResponse;
@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final AssignmentService service;
    private final AssessmentFeatureFlagsService featureFlagsService;
    private final AssessmentUsageService assessmentUsageService;

    public AssignmentController(AssignmentService service,
                                AssessmentFeatureFlagsService featureFlagsService,
                                AssessmentUsageService assessmentUsageService) {
        this.service = service;
        this.featureFlagsService = featureFlagsService;
        this.assessmentUsageService = assessmentUsageService;
    }

    // ================= CREATE =================
    @PreAuthorize("hasRole('TRAINER')")
    @PostMapping
    public ResponseEntity<AssignmentResponse> createAssignment(
            @RequestBody CreateAssignmentRequest request,
            Principal principal,
            HttpServletRequest httpRequest) {
        featureFlagsService.enforce(orgId(httpRequest), principal.getName(), AssessmentFeatureKeys.CREATE_ASSIGNMENT);
        String trainerEmail = principal.getName();
        AssignmentResponse response =
                service.createAssignment(request, trainerEmail, orgId(httpRequest));
        return ResponseEntity.ok(response);
    }

    // ================= GET BY BATCH (STUDENT + TRAINER) =================
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/student")
    public ResponseEntity<List<AssignmentResponse>> getStudentAssignments(
            Principal principal,
            HttpServletRequest httpRequest) {
        featureFlagsService.enforce(orgId(httpRequest), principal.getName(), AssessmentFeatureKeys.SUBMIT_ASSIGNMENT);
        return ResponseEntity.ok(
                service.getStudentAssignments(
                        principal.getName(),
                        orgId(httpRequest)
                )
        );
    }

    // ================= GET TRAINER ASSIGNMENTS =================
    @PreAuthorize("hasRole('TRAINER')")
    @GetMapping("/trainer")
    public ResponseEntity<List<AssignmentResponse>> getMyAssignments(
            Principal principal,
            HttpServletRequest httpRequest) {
        featureFlagsService.enforce(orgId(httpRequest), principal.getName(), AssessmentFeatureKeys.CREATE_ASSIGNMENT);
        String trainerEmail = principal.getName();
        return ResponseEntity.ok(
                service.getAssignmentsByTrainer(trainerEmail, orgId(httpRequest))
        );
    }

    // ================= UPDATE =================
    @PreAuthorize("hasRole('TRAINER')")
    @PutMapping("/{id}")
    public ResponseEntity<AssignmentResponse> updateAssignment(
            @PathVariable Long id,
            @RequestBody CreateAssignmentRequest request,
            Principal principal,
            HttpServletRequest httpRequest) {
        featureFlagsService.enforce(orgId(httpRequest), principal.getName(), AssessmentFeatureKeys.CREATE_ASSIGNMENT);
        String trainerEmail = principal.getName();
        AssignmentResponse updated =
                service.updateAssignment(id, request, trainerEmail, orgId(httpRequest));
        return ResponseEntity.ok(updated);
    }

    // ================= DELETE =================
    @PreAuthorize("hasRole('TRAINER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAssignment(
            @PathVariable Long id,
            Principal principal,
            HttpServletRequest httpRequest) {
        featureFlagsService.enforce(orgId(httpRequest), principal.getName(), AssessmentFeatureKeys.CREATE_ASSIGNMENT);
        String trainerEmail = principal.getName();
        service.deleteAssignment(id, trainerEmail, orgId(httpRequest));
        return ResponseEntity.ok("Assignment Deleted Successfully");
    }

    // ================= USAGE PREVIEW (TRAINER) =================
    // Read-only preview, not feature-gated.
    @PreAuthorize("hasRole('TRAINER')")
    @GetMapping("/usage/create")
    public ResponseEntity<Map<String, Object>> getCreateUsage(
            Principal principal,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(assessmentUsageService.getUsageStatus(
                AssessmentUsageLimits.Action.ASSIGNMENT_CREATE, principal.getName(), orgId(httpRequest)));
    }

    private String orgId(HttpServletRequest request) {
        return (String) request.getAttribute("organizationId");
    }

    // ── Feature-flag helper: email for endpoints with no Principal/Authentication param ──
    private String callerEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<List<AssignmentAdminReportResponse>> getAdminReport(HttpServletRequest httpRequest) {
        featureFlagsService.enforce(orgId(httpRequest), callerEmail(), AssessmentFeatureKeys.VIEW_ASSIGNMENT_ADMIN_REPORT);
        return ResponseEntity.ok(service.getAdminReport(orgId(httpRequest)));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/superadmin")
    public ResponseEntity<List<AssignmentAdminReportResponse>> getSuperAdminReport() {
        return ResponseEntity.ok(service.getSuperAdminReport());
    }
}