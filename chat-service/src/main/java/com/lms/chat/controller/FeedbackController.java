

package com.lms.chat.controller;

import com.lms.chat.constants.ChatFeatureKeys;
import com.lms.chat.dto.AlertConfigDTO;
import com.lms.chat.dto.FeedbackResponse;
import com.lms.chat.dto.FeedbackSummaryResponse;
import com.lms.chat.dto.SubmitFeedbackRequest;
import com.lms.chat.entity.ChatBatchTrainer;
import com.lms.chat.entity.Feedback;
import com.lms.chat.repository.ChatBatchTrainerRepository;
import com.lms.chat.repository.FeedbackRepository;
import com.lms.chat.service.AlertConfigService;
import com.lms.chat.service.ChatFeatureFlagsService;
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
    private final ChatBatchTrainerRepository chatBatchTrainerRepository;
    private final FeedbackRepository feedbackRepository;
    private final ChatFeatureFlagsService chatFeatureFlagsService;

    public FeedbackController(FeedbackService feedbackService,
                               AlertConfigService alertConfigService,
                               ChatBatchTrainerRepository chatBatchTrainerRepository,
                               FeedbackRepository feedbackRepository,
                               ChatFeatureFlagsService chatFeatureFlagsService) {
        this.feedbackService = feedbackService;
        this.alertConfigService = alertConfigService;
        this.chatBatchTrainerRepository = chatBatchTrainerRepository;
        this.feedbackRepository = feedbackRepository;
        this.chatFeatureFlagsService = chatFeatureFlagsService;
    }

    private String organizationId(Authentication auth) {
        Object details = auth.getDetails();
        return details == null ? null : details.toString();
    }

    private void enforceOrgAccess(Long batchId, Authentication auth) {
        String adminOrgId = organizationId(auth);
        if (adminOrgId == null) {
            return; // Super Admin — no restriction
        }
        String batchOrgId = chatBatchTrainerRepository
                .findByBatchId(batchId)
                .map(ChatBatchTrainer::getOrganizationId)
                .orElse(null);

        if (batchOrgId != null && !adminOrgId.equals(batchOrgId)) {
            throw new RuntimeException(
                    "Cross-organization access denied: batch does not belong to your organization");
        }
    }

    // ── Student endpoints ──────────────────────────────────────────

    @PostMapping("/submit")
    public ResponseEntity<?> submitFeedback(
            @RequestBody SubmitFeedbackRequest request,
            Authentication auth) {

        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.SUBMIT_FEEDBACK);

        try {
            String studentEmail = auth.getName();

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

    @GetMapping("/student/my")
    public ResponseEntity<List<FeedbackResponse>> getMyFeedback(Authentication auth) {
        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.GET_MY_FEEDBACK);
        return ResponseEntity.ok(feedbackService.getStudentFeedback(auth.getName()));
    }

    @GetMapping("/student/my/batch/{batchId}")
    public ResponseEntity<List<FeedbackResponse>> getMyFeedbackByBatch(
            @PathVariable Long batchId,
            Authentication auth) {
        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.GET_MY_FEEDBACK_BY_BATCH);
        return ResponseEntity.ok(feedbackService.getStudentFeedbackByBatch(auth.getName(), batchId));
    }

    // ── Trainer endpoints ──────────────────────────────────────────

    @GetMapping("/trainer/my")
    public ResponseEntity<List<FeedbackResponse>> getMyTrainerFeedback(Authentication auth) {
        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.GET_TRAINER_FEEDBACK);
        return ResponseEntity.ok(feedbackService.getTrainerFeedback(auth.getName()));
    }

    @GetMapping("/trainer/my/batch/{batchId}")
    public ResponseEntity<List<FeedbackResponse>> getMyTrainerFeedbackByBatch(
            @PathVariable Long batchId,
            Authentication auth) {
        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.GET_TRAINER_FEEDBACK_BY_BATCH);
        return ResponseEntity.ok(feedbackService.getTrainerFeedbackByBatch(auth.getName(), batchId));
    }

    @GetMapping("/trainer/my/batch/{batchId}/summary")
    public ResponseEntity<FeedbackSummaryResponse> getMyTrainerSummary(
            @PathVariable Long batchId,
            Authentication auth) {
        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.GET_TRAINER_FEEDBACK_SUMMARY);
        return ResponseEntity.ok(feedbackService.getTrainerSummary(auth.getName(), batchId));
    }

    // ── Admin endpoints ────────────────────────────────────────────

    @GetMapping("/admin/batch/{batchId}")
    public ResponseEntity<List<FeedbackResponse>> getBatchFeedback(
            @PathVariable Long batchId,
            Authentication auth) {
        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.GET_BATCH_FEEDBACK);
        enforceOrgAccess(batchId, auth);
        return ResponseEntity.ok(feedbackService.getBatchFeedback(batchId));
    }

    @GetMapping("/admin/batch/{batchId}/summaries")
    public ResponseEntity<List<FeedbackSummaryResponse>> getBatchSummaries(
            @PathVariable Long batchId,
            Authentication auth) {
        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.GET_BATCH_SUMMARIES);
        enforceOrgAccess(batchId, auth);
        return ResponseEntity.ok(feedbackService.getBatchSummaries(batchId));
    }

    @PatchMapping("/admin/{feedbackId}/status")
    public ResponseEntity<FeedbackResponse> updateFeedbackStatus(
            @PathVariable Long feedbackId,
            @RequestBody Map<String, String> body,
            Authentication auth) {

        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.UPDATE_FEEDBACK_STATUS);

        String status = body.get("status");
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Long batchId = feedbackRepository.findById(feedbackId)
                .map(Feedback::getBatchId)
                .orElseThrow(() -> new RuntimeException("Feedback not found: " + feedbackId));
        enforceOrgAccess(batchId, auth);

        return ResponseEntity.ok(feedbackService.updateStatus(feedbackId, status));
    }

    @GetMapping("/check/{batchId}")
    public ResponseEntity<?> checkFeedbackStatus(
            @PathVariable Long batchId,
            Authentication auth) {

        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.CHECK_FEEDBACK_STATUS);

        String studentEmail = auth.getName();
        boolean hasSubmitted = feedbackService.hasFeedback(studentEmail, batchId);

        return ResponseEntity.ok(Map.of(
                "alreadySubmitted", hasSubmitted,
                "message", hasSubmitted
                    ? "You already submitted feedback for this batch"
                    : "Ready to submit"
        ));
    }

    // ✅ ============ ALERT CONFIG ENDPOINTS ============

    @PostMapping("/alert-config")
    public ResponseEntity<AlertConfigDTO> createOrUpdateAlertConfig(
            @RequestBody AlertConfigDTO dto,
            Authentication auth) {
        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.CREATE_UPDATE_ALERT_CONFIG);
        enforceOrgAccess(dto.getBatchId(), auth);
        return ResponseEntity.ok(alertConfigService.createOrUpdateAlertConfig(dto));
    }

    @GetMapping("/alert-config/{batchId}")
    public ResponseEntity<AlertConfigDTO> getAlertConfig(
            @PathVariable Long batchId,
            Authentication auth) {
        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.GET_ALERT_CONFIG);
        enforceOrgAccess(batchId, auth);
        return ResponseEntity.ok(alertConfigService.getAlertConfig(batchId));
    }

    @DeleteMapping("/alert-config/{batchId}")
    public ResponseEntity<Void> deleteAlertConfig(
            @PathVariable Long batchId,
            Authentication auth) {
        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.DELETE_ALERT_CONFIG);
        enforceOrgAccess(batchId, auth);
        alertConfigService.deleteAlertConfig(batchId);
        return ResponseEntity.noContent().build();
    }

    // ══════════════════════════════════════════════════════════════
    // SUPER ADMIN — UNCHANGED, NO FEATURE ENFORCEMENT BELOW THIS LINE
    // ══════════════════════════════════════════════════════════════

    private void enforceOrglessBatch(Long batchId) {
        String batchOrgId = chatBatchTrainerRepository
                .findByBatchId(batchId)
                .map(ChatBatchTrainer::getOrganizationId)
                .orElse(null);

        if (batchOrgId != null) {
            throw new RuntimeException(
                    "Batch " + batchId + " belongs to an organization and is not "
                    + "accessible via Super Admin unassigned-batch endpoints");
        }
    }

    @GetMapping("/super-admin/batches")
    public ResponseEntity<List<Long>> getSuperAdminOrglessBatchIds() {
        return ResponseEntity.ok(feedbackService.getOrglessBatchIds());
    }

    @GetMapping("/super-admin/feedback")
    public ResponseEntity<List<FeedbackResponse>> getSuperAdminFeedback() {
        return ResponseEntity.ok(feedbackService.getFeedbackForOrglessBatches());
    }

    @GetMapping("/super-admin/summaries")
    public ResponseEntity<List<FeedbackSummaryResponse>> getSuperAdminSummaries() {
        return ResponseEntity.ok(feedbackService.getSummariesForOrglessBatches());
    }

    @GetMapping("/super-admin/batch/{batchId}")
    public ResponseEntity<List<FeedbackResponse>> getSuperAdminBatchFeedback(
            @PathVariable Long batchId) {
        enforceOrglessBatch(batchId);
        return ResponseEntity.ok(feedbackService.getBatchFeedback(batchId));
    }

    @GetMapping("/super-admin/batch/{batchId}/summaries")
    public ResponseEntity<List<FeedbackSummaryResponse>> getSuperAdminBatchSummaries(
            @PathVariable Long batchId) {
        enforceOrglessBatch(batchId);
        return ResponseEntity.ok(feedbackService.getBatchSummaries(batchId));
    }

    @PatchMapping("/super-admin/{feedbackId}/status")
    public ResponseEntity<FeedbackResponse> updateSuperAdminFeedbackStatus(
            @PathVariable Long feedbackId,
            @RequestBody Map<String, String> body) {

        String status = body.get("status");
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Long batchId = feedbackRepository.findById(feedbackId)
                .map(Feedback::getBatchId)
                .orElseThrow(() -> new RuntimeException("Feedback not found: " + feedbackId));
        enforceOrglessBatch(batchId);

        return ResponseEntity.ok(feedbackService.updateStatus(feedbackId, status));
    }

    @PostMapping("/super-admin/alert-config")
    public ResponseEntity<AlertConfigDTO> createOrUpdateSuperAdminAlertConfig(
            @RequestBody AlertConfigDTO dto) {
        enforceOrglessBatch(dto.getBatchId());
        return ResponseEntity.ok(alertConfigService.createOrUpdateAlertConfig(dto));
    }

    @GetMapping("/super-admin/alert-config/{batchId}")
    public ResponseEntity<AlertConfigDTO> getSuperAdminAlertConfig(
            @PathVariable Long batchId) {
        enforceOrglessBatch(batchId);
        return ResponseEntity.ok(alertConfigService.getAlertConfig(batchId));
    }

    @DeleteMapping("/super-admin/alert-config/{batchId}")
    public ResponseEntity<Void> deleteSuperAdminAlertConfig(
            @PathVariable Long batchId) {
        enforceOrglessBatch(batchId);
        alertConfigService.deleteAlertConfig(batchId);
        return ResponseEntity.noContent().build();
    }
}