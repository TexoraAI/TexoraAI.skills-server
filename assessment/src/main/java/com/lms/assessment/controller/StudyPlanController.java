//
//
//package com.lms.assessment.controller;
//
//import com.lms.assessment.dto.StudyPlanRequest;
//import com.lms.assessment.dto.StudyPlanResponse;
//import com.lms.assessment.service.StudyPlanService;
//import jakarta.servlet.http.HttpServletRequest;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.*;
//import com.lms.assessment.dto.StudyPlanAdminReportResponse;
//import org.springframework.security.access.prepost.PreAuthorize;
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/v1/study-plans")
//public class StudyPlanController {
//
//    private final StudyPlanService studyPlanService;
//
//    public StudyPlanController(StudyPlanService studyPlanService) {
//        this.studyPlanService = studyPlanService;
//    }
//
//    /* ══════════════════════════════════════════════
//       TRAINER ENDPOINTS
//       ══════════════════════════════════════════════ */
//
//    @PostMapping
//    public ResponseEntity<?> createStudyPlan(
//            @RequestBody StudyPlanRequest request,
//            Authentication auth,
//            HttpServletRequest httpRequest) {
//        try {
//            String trainerEmail = auth.getName();
//            if (request.getTitle() == null || request.getTitle().isBlank()) {
//                return ResponseEntity.badRequest()
//                        .body(Map.of("message", "Title is required."));
//            }
//            StudyPlanResponse response = studyPlanService.createStudyPlan(request, trainerEmail, orgId(httpRequest));
//            return ResponseEntity.status(HttpStatus.CREATED).body(response);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of("message", e.getMessage()));
//        }
//    }
//
//    @GetMapping("/my")
//    public ResponseEntity<List<StudyPlanResponse>> getMyPlans(Authentication auth, HttpServletRequest httpRequest) {
//        return ResponseEntity.ok(studyPlanService.getMyPlans(auth.getName(), orgId(httpRequest)));
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<?> getPlanById(
//            @PathVariable Long id,
//            Authentication auth,
//            HttpServletRequest httpRequest) {
//        try {
//            return ResponseEntity.ok(studyPlanService.getPlanById(id, auth.getName(), orgId(httpRequest)));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(Map.of("message", e.getMessage()));
//        }
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<?> updateStudyPlan(
//            @PathVariable Long id,
//            @RequestBody StudyPlanRequest request,
//            Authentication auth,
//            HttpServletRequest httpRequest) {
//        try {
//            return ResponseEntity.ok(
//                    studyPlanService.updateStudyPlan(id, request, auth.getName(), orgId(httpRequest)));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(Map.of("message", e.getMessage()));
//        }
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<?> deleteStudyPlan(
//            @PathVariable Long id,
//            Authentication auth,
//            HttpServletRequest httpRequest) {
//        try {
//            studyPlanService.deleteStudyPlan(id, auth.getName(), orgId(httpRequest));
//            return ResponseEntity.ok(Map.of("message", "Study plan deleted successfully."));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(Map.of("message", e.getMessage()));
//        }
//    }
//
//    @PatchMapping("/{id}/toggle-active")
//    public ResponseEntity<?> toggleActive(
//            @PathVariable Long id,
//            Authentication auth,
//            HttpServletRequest httpRequest) {
//        try {
//            return ResponseEntity.ok(studyPlanService.toggleActive(id, auth.getName(), orgId(httpRequest)));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(Map.of("message", e.getMessage()));
//        }
//    }
//
//    /* ══════════════════════════════════════════════
//       STUDENT ENDPOINTS
//       ══════════════════════════════════════════════ */
//
//    @GetMapping("/student")
//    public ResponseEntity<List<StudyPlanResponse>> getStudentPlans(
//            @RequestParam Long batchId,
//            Authentication auth,
//            HttpServletRequest httpRequest) {
//        return ResponseEntity.ok(
//                studyPlanService.getStudentPlans(batchId, auth.getName(), orgId(httpRequest)));
//    }
//
//    @GetMapping("/student/{id}")
//    public ResponseEntity<?> getStudentPlanById(
//            @PathVariable Long id,
//            Authentication auth,
//            HttpServletRequest httpRequest) {
//        try {
//            return ResponseEntity.ok(
//                    studyPlanService.getStudentPlanById(id, auth.getName(), orgId(httpRequest)));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(Map.of("message", e.getMessage()));
//        }
//    }
//
//    @PostMapping("/progress/mark")
//    public ResponseEntity<?> markProgress(
//            @RequestBody Map<String, Object> body,
//            Authentication auth) {
//        try {
//            Long studyPlanItemId = Long.valueOf(body.get("studyPlanItemId").toString());
//            Long batchId         = Long.valueOf(body.get("batchId").toString());
//            Long problemId       = Long.valueOf(body.get("problemId").toString());
//            int  marksObtained   = Integer.parseInt(body.get("marksObtained").toString());
//
//            studyPlanService.markItemComplete(
//                    studyPlanItemId, auth.getName(), batchId, problemId, marksObtained);
//
//            return ResponseEntity.ok(Map.of("message", "Progress recorded."));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(Map.of("message", e.getMessage()));
//        }
//    }
//
//    private String orgId(HttpServletRequest request) {
//        return (String) request.getAttribute("organizationId");
//    }
//    @GetMapping("/admin")
//    @PreAuthorize("hasRole('TENANT_ADMIN')")
//    public ResponseEntity<List<StudyPlanAdminReportResponse>> getAdminReport(HttpServletRequest httpRequest) {
//        return ResponseEntity.ok(studyPlanService.getAdminReport(orgId(httpRequest)));
//    }
//
//    @GetMapping("/superadmin")
//    @PreAuthorize("hasRole('SUPER_ADMIN')")
//    public ResponseEntity<List<StudyPlanAdminReportResponse>> getSuperAdminReport() {
//        return ResponseEntity.ok(studyPlanService.getSuperAdminReport());
//    }
//    @GetMapping("/admin/{id}/items")
//    @PreAuthorize("hasRole('TENANT_ADMIN')")
//    public ResponseEntity<?> getPlanItemsForAdmin(@PathVariable Long id, HttpServletRequest httpRequest) {
//        try {
//            return ResponseEntity.ok(studyPlanService.getPlanItemsForAdmin(id, orgId(httpRequest)));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
//        }
//    }
//    @GetMapping("/superadmin/{id}/items")
//    @PreAuthorize("hasRole('SUPER_ADMIN')")
//    public ResponseEntity<?> getPlanItemsForSuperAdmin(@PathVariable Long id) {
//        try {
//            return ResponseEntity.ok(studyPlanService.getPlanItemsForSuperAdmin(id));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
//        }
//    }
//}





package com.lms.assessment.controller;

import com.lms.assessment.constants.AssessmentFeatureKeys;
import com.lms.assessment.dto.StudyPlanRequest;
import com.lms.assessment.dto.StudyPlanResponse;
import com.lms.assessment.service.AssessmentFeatureFlagsService;
import com.lms.assessment.service.StudyPlanService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.lms.assessment.dto.StudyPlanAdminReportResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/study-plans")
public class StudyPlanController {

    private final StudyPlanService studyPlanService;
    private final AssessmentFeatureFlagsService featureFlagsService;

    public StudyPlanController(StudyPlanService studyPlanService,
                               AssessmentFeatureFlagsService featureFlagsService) {
        this.studyPlanService = studyPlanService;
        this.featureFlagsService = featureFlagsService;
    }

    /* ══════════════════════════════════════════════
       TRAINER ENDPOINTS
       ══════════════════════════════════════════════ */

    @PostMapping
    public ResponseEntity<?> createStudyPlan(
            @RequestBody StudyPlanRequest request,
            Authentication auth,
            HttpServletRequest httpRequest) {
        try {
            featureFlagsService.enforce(orgId(httpRequest), auth.getName(), AssessmentFeatureKeys.CREATE_STUDY_PLAN);
            String trainerEmail = auth.getName();
            if (request.getTitle() == null || request.getTitle().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Title is required."));
            }
            StudyPlanResponse response = studyPlanService.createStudyPlan(request, trainerEmail, orgId(httpRequest));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<List<StudyPlanResponse>> getMyPlans(Authentication auth, HttpServletRequest httpRequest) {
        featureFlagsService.enforce(orgId(httpRequest), auth.getName(), AssessmentFeatureKeys.CREATE_STUDY_PLAN);
        return ResponseEntity.ok(studyPlanService.getMyPlans(auth.getName(), orgId(httpRequest)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPlanById(
            @PathVariable Long id,
            Authentication auth,
            HttpServletRequest httpRequest) {
        try {
            featureFlagsService.enforce(orgId(httpRequest), auth.getName(), AssessmentFeatureKeys.CREATE_STUDY_PLAN);
            return ResponseEntity.ok(studyPlanService.getPlanById(id, auth.getName(), orgId(httpRequest)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudyPlan(
            @PathVariable Long id,
            @RequestBody StudyPlanRequest request,
            Authentication auth,
            HttpServletRequest httpRequest) {
        try {
            featureFlagsService.enforce(orgId(httpRequest), auth.getName(), AssessmentFeatureKeys.CREATE_STUDY_PLAN);
            return ResponseEntity.ok(
                    studyPlanService.updateStudyPlan(id, request, auth.getName(), orgId(httpRequest)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudyPlan(
            @PathVariable Long id,
            Authentication auth,
            HttpServletRequest httpRequest) {
        try {
            featureFlagsService.enforce(orgId(httpRequest), auth.getName(), AssessmentFeatureKeys.CREATE_STUDY_PLAN);
            studyPlanService.deleteStudyPlan(id, auth.getName(), orgId(httpRequest));
            return ResponseEntity.ok(Map.of("message", "Study plan deleted successfully."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<?> toggleActive(
            @PathVariable Long id,
            Authentication auth,
            HttpServletRequest httpRequest) {
        try {
            featureFlagsService.enforce(orgId(httpRequest), auth.getName(), AssessmentFeatureKeys.CREATE_STUDY_PLAN);
            return ResponseEntity.ok(studyPlanService.toggleActive(id, auth.getName(), orgId(httpRequest)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /* ══════════════════════════════════════════════
       STUDENT ENDPOINTS
       ══════════════════════════════════════════════ */

    @GetMapping("/student")
    public ResponseEntity<List<StudyPlanResponse>> getStudentPlans(
            @RequestParam Long batchId,
            Authentication auth,
            HttpServletRequest httpRequest) {
        featureFlagsService.enforce(orgId(httpRequest), auth.getName(), AssessmentFeatureKeys.ACCESS_STUDY_PLAN);
        return ResponseEntity.ok(
                studyPlanService.getStudentPlans(batchId, auth.getName(), orgId(httpRequest)));
    }

    @GetMapping("/student/{id}")
    public ResponseEntity<?> getStudentPlanById(
            @PathVariable Long id,
            Authentication auth,
            HttpServletRequest httpRequest) {
        try {
            featureFlagsService.enforce(orgId(httpRequest), auth.getName(), AssessmentFeatureKeys.ACCESS_STUDY_PLAN);
            return ResponseEntity.ok(
                    studyPlanService.getStudentPlanById(id, auth.getName(), orgId(httpRequest)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/progress/mark")
    public ResponseEntity<?> markProgress(
            @RequestBody Map<String, Object> body,
            Authentication auth,
            HttpServletRequest httpRequest) {
        try {
            featureFlagsService.enforce(orgId(httpRequest), auth.getName(), AssessmentFeatureKeys.ACCESS_STUDY_PLAN);
            Long studyPlanItemId = Long.valueOf(body.get("studyPlanItemId").toString());
            Long batchId         = Long.valueOf(body.get("batchId").toString());
            Long problemId       = Long.valueOf(body.get("problemId").toString());
            int  marksObtained   = Integer.parseInt(body.get("marksObtained").toString());

            studyPlanService.markItemComplete(
                    studyPlanItemId, auth.getName(), batchId, problemId, marksObtained);

            return ResponseEntity.ok(Map.of("message", "Progress recorded."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    private String orgId(HttpServletRequest request) {
        return (String) request.getAttribute("organizationId");
    }

    // ── Feature-flag helper: admin report endpoints have no Authentication param ──
    private String callerEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<List<StudyPlanAdminReportResponse>> getAdminReport(HttpServletRequest httpRequest) {
        featureFlagsService.enforce(orgId(httpRequest), callerEmail(), AssessmentFeatureKeys.VIEW_STUDY_PLAN_ADMIN_REPORT);
        return ResponseEntity.ok(studyPlanService.getAdminReport(orgId(httpRequest)));
    }

    @GetMapping("/superadmin")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<StudyPlanAdminReportResponse>> getSuperAdminReport() {
        return ResponseEntity.ok(studyPlanService.getSuperAdminReport());
    }
    @GetMapping("/admin/{id}/items")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<?> getPlanItemsForAdmin(@PathVariable Long id, HttpServletRequest httpRequest) {
        try {
            // Same capability/key as the admin report above — no separate toggle.
            featureFlagsService.enforce(orgId(httpRequest), callerEmail(), AssessmentFeatureKeys.VIEW_STUDY_PLAN_ADMIN_REPORT);
            return ResponseEntity.ok(studyPlanService.getPlanItemsForAdmin(id, orgId(httpRequest)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }
    @GetMapping("/superadmin/{id}/items")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getPlanItemsForSuperAdmin(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(studyPlanService.getPlanItemsForSuperAdmin(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }
}