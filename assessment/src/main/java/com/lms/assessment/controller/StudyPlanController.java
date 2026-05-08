package com.lms.assessment.controller;

import com.lms.assessment.dto.StudyPlanRequest;
import com.lms.assessment.dto.StudyPlanResponse;
import com.lms.assessment.service.StudyPlanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * StudyPlanController
 *
 * Base path: /api/v1/study-plans
 *
 * TRAINER endpoints (JWT role = TRAINER):
 *   POST   /api/v1/study-plans                       → create plan
 *   GET    /api/v1/study-plans/my                    → my plans
 *   GET    /api/v1/study-plans/{id}                  → single plan
 *   PUT    /api/v1/study-plans/{id}                  → update plan
 *   DELETE /api/v1/study-plans/{id}                  → delete plan
 *   PATCH  /api/v1/study-plans/{id}/toggle-active    → activate/deactivate
 *
 * STUDENT endpoints (JWT role = STUDENT):
 *   GET    /api/v1/study-plans/student?batchId=...   → batch plans with progress
 *   GET    /api/v1/study-plans/student/{id}          → single plan with progress
 */
@RestController
@RequestMapping("/api/v1/study-plans")
public class StudyPlanController {

    private final StudyPlanService studyPlanService;

    public StudyPlanController(StudyPlanService studyPlanService) {
        this.studyPlanService = studyPlanService;
    }

    /* ══════════════════════════════════════════════
       TRAINER ENDPOINTS
       ══════════════════════════════════════════════ */

    /**
     * POST /api/v1/study-plans
     * Create a new study plan (trainerEmail from JWT)
     */
    @PostMapping
    public ResponseEntity<?> createStudyPlan(
            @RequestBody StudyPlanRequest request,
            Authentication auth) {
        try {
            String trainerEmail = auth.getName();
            if (request.getTitle() == null || request.getTitle().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Title is required."));
            }
            StudyPlanResponse response = studyPlanService.createStudyPlan(request, trainerEmail);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * GET /api/v1/study-plans/my
     * Trainer: get all plans created by logged-in trainer
     */
    @GetMapping("/my")
    public ResponseEntity<List<StudyPlanResponse>> getMyPlans(Authentication auth) {
        return ResponseEntity.ok(studyPlanService.getMyPlans(auth.getName()));
    }

    /**
     * GET /api/v1/study-plans/{id}
     * Trainer: get single plan by ID (must own it)
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPlanById(
            @PathVariable Long id,
            Authentication auth) {
        try {
            return ResponseEntity.ok(studyPlanService.getPlanById(id, auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * PUT /api/v1/study-plans/{id}
     * Trainer: update a plan (must own it)
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudyPlan(
            @PathVariable Long id,
            @RequestBody StudyPlanRequest request,
            Authentication auth) {
        try {
            return ResponseEntity.ok(
                    studyPlanService.updateStudyPlan(id, request, auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * DELETE /api/v1/study-plans/{id}
     * Trainer: delete a plan (must own it)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudyPlan(
            @PathVariable Long id,
            Authentication auth) {
        try {
            studyPlanService.deleteStudyPlan(id, auth.getName());
            return ResponseEntity.ok(Map.of("message", "Study plan deleted successfully."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * PATCH /api/v1/study-plans/{id}/toggle-active
     * Trainer: toggle active/inactive
     */
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<?> toggleActive(
            @PathVariable Long id,
            Authentication auth) {
        try {
            return ResponseEntity.ok(studyPlanService.toggleActive(id, auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /* ══════════════════════════════════════════════
       STUDENT ENDPOINTS
       ══════════════════════════════════════════════ */

    /**
     * GET /api/v1/study-plans/student?batchId=...
     * Student: get all active plans for their batch with progress
     */
    @GetMapping("/student")
    public ResponseEntity<List<StudyPlanResponse>> getStudentPlans(
            @RequestParam Long batchId,
            Authentication auth) {
        return ResponseEntity.ok(
                studyPlanService.getStudentPlans(batchId, auth.getName()));
    }

    /**
     * GET /api/v1/study-plans/student/{id}
     * Student: get single plan with full progress
     */
    @GetMapping("/student/{id}")
    public ResponseEntity<?> getStudentPlanById(
            @PathVariable Long id,
            Authentication auth) {
        try {
            return ResponseEntity.ok(
                    studyPlanService.getStudentPlanById(id, auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * POST /api/v1/study-plans/progress/mark
     * Internal or student hook: mark a problem complete in a plan
     * Body: { studyPlanItemId, batchId, problemId, marksObtained }
     */
    @PostMapping("/progress/mark")
    public ResponseEntity<?> markProgress(
            @RequestBody Map<String, Object> body,
            Authentication auth) {
        try {
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
}