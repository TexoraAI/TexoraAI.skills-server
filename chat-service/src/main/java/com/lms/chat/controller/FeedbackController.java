package com.lms.chat.controller;

import com.lms.chat.dto.AlertConfigDTO;
import com.lms.chat.dto.FeedbackResponse;
import com.lms.chat.dto.FeedbackSummaryResponse;
import com.lms.chat.dto.SubmitFeedbackRequest;
import com.lms.chat.service.AlertConfigService;
import com.lms.chat.service.FeedbackService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final AlertConfigService alertConfigService;

    public FeedbackController(FeedbackService feedbackService, AlertConfigService alertConfigService) {
        this.feedbackService = feedbackService;
        this.alertConfigService = alertConfigService;
    }

    // ── Student endpoints ──────────────────────────────────────────

    /**
     * POST /api/feedback/submit
     * studentEmail resolved from JWT — frontend only sends batchId + trainerEmail
     */
//    @PostMapping("/submit")
//    public ResponseEntity<FeedbackResponse> submitFeedback(
//            @RequestBody SubmitFeedbackRequest request,
//            Authentication auth) {
//
//        request.setStudentEmail(auth.getName());
//        return ResponseEntity.ok(feedbackService.submitFeedback(request));
//    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitFeedback(
            @RequestBody SubmitFeedbackRequest request,
            Authentication auth) {

        try {
            String studentEmail = auth.getName();
            
            // ✅ Check if already submitted
            if (feedbackService.hasFeedback(studentEmail, request.getBatchId())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                        Map.of(
                            "code", "FEEDBACK_ALREADY_SUBMITTED",
                            "message", "You have already submitted feedback for this batch. You can only submit once per batch.",
                            "status", 409
                        )
                );
            }

            request.setStudentEmail(studentEmail);
            return ResponseEntity.ok(feedbackService.submitFeedback(request));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of(
                        "code", "SUBMISSION_FAILED",
                        "message", e.getMessage(),
                        "status", 500
                    )
            );
        }
    }
    
    
    /**
     * GET /api/feedback/student/my
     * Student views all their own feedback (email from JWT)
     */
    @GetMapping("/student/my")
    public ResponseEntity<List<FeedbackResponse>> getMyFeedback(Authentication auth) {
        return ResponseEntity.ok(
                feedbackService.getStudentFeedback(auth.getName()));
    }
    
    

    /**
     * GET /api/feedback/student/my/batch/{batchId}
     * Student views their feedback for a specific batch
     */
    @GetMapping("/student/my/batch/{batchId}")
    public ResponseEntity<List<FeedbackResponse>> getMyFeedbackByBatch(
            @PathVariable Long batchId,
            Authentication auth) {

        return ResponseEntity.ok(
                feedbackService.getStudentFeedbackByBatch(auth.getName(), batchId));
    }

    // ── Trainer endpoints ──────────────────────────────────────────

    /**
     * GET /api/feedback/trainer/my
     * Trainer views all feedback received (email from JWT)
     */
    @GetMapping("/trainer/my")
    public ResponseEntity<List<FeedbackResponse>> getMyTrainerFeedback(Authentication auth) {
        return ResponseEntity.ok(
                feedbackService.getTrainerFeedback(auth.getName()));
    }

    /**
     * GET /api/feedback/trainer/my/batch/{batchId}
     */
    @GetMapping("/trainer/my/batch/{batchId}")
    public ResponseEntity<List<FeedbackResponse>> getMyTrainerFeedbackByBatch(
            @PathVariable Long batchId,
            Authentication auth) {

        return ResponseEntity.ok(
                feedbackService.getTrainerFeedbackByBatch(auth.getName(), batchId));
    }

    /**
     * GET /api/feedback/trainer/my/batch/{batchId}/summary
     */
    @GetMapping("/trainer/my/batch/{batchId}/summary")
    public ResponseEntity<FeedbackSummaryResponse> getMyTrainerSummary(
            @PathVariable Long batchId,
            Authentication auth) {

        return ResponseEntity.ok(
                feedbackService.getTrainerSummary(auth.getName(), batchId));
    }

    // ── Admin endpoints ────────────────────────────────────────────

    /**
     * GET /api/feedback/admin/batch/{batchId}
     */
    @GetMapping("/admin/batch/{batchId}")
    public ResponseEntity<List<FeedbackResponse>> getBatchFeedback(
            @PathVariable Long batchId) {

        return ResponseEntity.ok(feedbackService.getBatchFeedback(batchId));
    }

    /**
     * GET /api/feedback/admin/batch/{batchId}/summaries
     */
    @GetMapping("/admin/batch/{batchId}/summaries")
    public ResponseEntity<List<FeedbackSummaryResponse>> getBatchSummaries(
            @PathVariable Long batchId) {

        return ResponseEntity.ok(feedbackService.getBatchSummaries(batchId));
    }

    /**
     * PATCH /api/feedback/admin/{feedbackId}/status
     * Body: { "status": "REVIEWED" }
     */
    @PatchMapping("/admin/{feedbackId}/status")
    public ResponseEntity<FeedbackResponse> updateFeedbackStatus(
            @PathVariable Long feedbackId,
            @RequestBody Map<String, String> body) {

        String status = body.get("status");
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(feedbackService.updateStatus(feedbackId, status));
    }
    /**
     * ✅ NEW: GET /api/feedback/check/{batchId}
     * Check if student already submitted feedback for a batch
     */
    @GetMapping("/check/{batchId}")
    public ResponseEntity<?> checkFeedbackStatus(
            @PathVariable Long batchId,
            Authentication auth) {

        String studentEmail = auth.getName();
        boolean hasSubmitted = feedbackService.hasFeedback(studentEmail, batchId);

        return ResponseEntity.ok(Map.of(
                "alreadySubmitted", hasSubmitted,
                "message", hasSubmitted 
                    ? "You already submitted feedback for this batch" 
                    : "Ready to submit"
        ));
    }
    

    // ✅ ============ NEW: ALERT CONFIG ENDPOINTS ============

    /**
     * POST /api/feedback/alert-config
     * Create or update alert configuration for a batch
     */
    @PostMapping("/alert-config")
    public ResponseEntity<AlertConfigDTO> createOrUpdateAlertConfig(
            @RequestBody AlertConfigDTO dto) {
        return ResponseEntity.ok(alertConfigService.createOrUpdateAlertConfig(dto));
    }

    /**
     * GET /api/feedback/alert-config/{batchId}
     * Get alert configuration for a batch
     */
    @GetMapping("/alert-config/{batchId}")
    public ResponseEntity<AlertConfigDTO> getAlertConfig(
            @PathVariable Long batchId) {
        return ResponseEntity.ok(alertConfigService.getAlertConfig(batchId));
    }

    /**
     * DELETE /api/feedback/alert-config/{batchId}
     * Delete alert configuration for a batch
     */
    @DeleteMapping("/alert-config/{batchId}")
    public ResponseEntity<Void> deleteAlertConfig(
            @PathVariable Long batchId) {
        alertConfigService.deleteAlertConfig(batchId);
        return ResponseEntity.noContent().build();
    }
    
    
    
}