package com.lms.chat.service;

import com.lms.chat.dto.FeedbackResponse;
import com.lms.chat.dto.FeedbackSummaryResponse;
import com.lms.chat.dto.SubmitFeedbackRequest;
import com.lms.chat.entity.Feedback;
import com.lms.chat.entity.FeedbackSummary;
import com.lms.chat.kafka.FeedbackEventProducer;
import com.lms.chat.repository.FeedbackRepository;
import com.lms.chat.repository.FeedbackSummaryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeedbackService {

    private static final Logger log = LoggerFactory.getLogger(FeedbackService.class);

    private final FeedbackRepository        feedbackRepository;
    private final FeedbackSummaryRepository summaryRepository;
    private final FeedbackEventProducer     eventProducer;

    public FeedbackService(FeedbackRepository feedbackRepository,
                           FeedbackSummaryRepository summaryRepository,
                           FeedbackEventProducer eventProducer) {
        this.feedbackRepository = feedbackRepository;
        this.summaryRepository  = summaryRepository;
        this.eventProducer      = eventProducer;
    }

    // ── Student: Submit ────────────────────────────────────────────

    @Transactional
    public FeedbackResponse submitFeedback(SubmitFeedbackRequest request) {
        // One feedback per student per batch (no sessionId)
        if (feedbackRepository.existsByStudentEmailAndBatchId(
                request.getStudentEmail(), request.getBatchId())) {
            throw new IllegalStateException(
                "Feedback already submitted for batch " + request.getBatchId()
                + " by " + request.getStudentEmail());
        }

        Feedback saved = feedbackRepository.save(mapToEntity(request));

        recomputeSummary(saved.getTrainerEmail(), saved.getBatchId());

        try {
            eventProducer.publishFeedbackSubmitted(saved);
        } catch (Exception e) {
            log.warn("Kafka down. Feedback saved without event: {}", e.getMessage());
        }

        log.info("Feedback submitted: id={} batch={} trainer={} anonymous={}",
                 saved.getId(), saved.getBatchId(),
                 saved.getTrainerEmail(), saved.isAnonymous());

        return FeedbackResponse.from(saved, false);
    }

    // ── Student: View own ─────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<FeedbackResponse> getStudentFeedback(String studentEmail) {
        return feedbackRepository
                .findByStudentEmailOrderByCreatedAtDesc(studentEmail)
                .stream()
                .map(f -> FeedbackResponse.from(f, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponse> getStudentFeedbackByBatch(
            String studentEmail, Long batchId) {
        return feedbackRepository
                .findByStudentEmailAndBatchIdOrderByCreatedAtDesc(studentEmail, batchId)
                .stream()
                .map(f -> FeedbackResponse.from(f, false))
                .collect(Collectors.toList());
    }

    // ── Trainer: View received ─────────────────────────────────────

    @Transactional(readOnly = true)
    public List<FeedbackResponse> getTrainerFeedback(String trainerEmail) {
        return feedbackRepository
                .findByTrainerEmailOrderByCreatedAtDesc(trainerEmail)
                .stream()
                .map(f -> FeedbackResponse.from(f, true))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponse> getTrainerFeedbackByBatch(
            String trainerEmail, Long batchId) {
        return feedbackRepository
                .findByTrainerEmailAndBatchIdOrderByCreatedAtDesc(trainerEmail, batchId)
                .stream()
                .map(f -> FeedbackResponse.from(f, true))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FeedbackSummaryResponse getTrainerSummary(
            String trainerEmail, Long batchId) {
        FeedbackSummary summary = summaryRepository
                .findByTrainerEmailAndBatchId(trainerEmail, batchId)
                .orElseThrow(() -> new RuntimeException(
                    "No summary found for trainer=" + trainerEmail
                    + " batch=" + batchId));
        return FeedbackSummaryResponse.from(summary);
    }

    // ── Admin ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<FeedbackResponse> getBatchFeedback(Long batchId) {
        return feedbackRepository
                .findByBatchIdOrderByCreatedAtDesc(batchId)
                .stream()
                .map(f -> FeedbackResponse.from(f, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FeedbackSummaryResponse> getBatchSummaries(Long batchId) {
        return summaryRepository
                .findByBatchIdOrderByOverallAvgRatingDesc(batchId)
                .stream()
                .map(FeedbackSummaryResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public FeedbackResponse updateStatus(Long feedbackId, String status) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException(
                    "Feedback not found: " + feedbackId));
        feedback.setStatus(Feedback.FeedbackStatus.valueOf(status.toUpperCase()));
        return FeedbackResponse.from(feedbackRepository.save(feedback), false);
    }

    // ── Private helpers ────────────────────────────────────────────

    private void recomputeSummary(String trainerEmail, Long batchId) {
        FeedbackSummary summary = summaryRepository
                .findByTrainerEmailAndBatchId(trainerEmail, batchId)
                .orElseGet(() -> {
                    FeedbackSummary s = new FeedbackSummary();
                    s.setTrainerEmail(trainerEmail);
                    s.setBatchId(batchId);
                    return s;
                });

        long count = feedbackRepository.countByTrainerAndBatch(trainerEmail, batchId);
        summary.setTotalFeedbackCount((int) count);
        summary.setAvgMoodScore(
                feedbackRepository.avgMoodScoreByTrainerAndBatch(trainerEmail, batchId));
        summary.setAvgClarityRating(
                feedbackRepository.avgClarityByTrainerAndBatch(trainerEmail, batchId));
        summary.setAvgDoubtClearingRating(
                feedbackRepository.avgDoubtClearingByTrainerAndBatch(trainerEmail, batchId));
        summary.setAvgEnergyRating(
                feedbackRepository.avgEnergyByTrainerAndBatch(trainerEmail, batchId));
        summary.setAvgTechnicalDepthRating(
                feedbackRepository.avgTechnicalDepthByTrainerAndBatch(trainerEmail, batchId));

        double total = 0; int parts = 0;
        if (summary.getAvgClarityRating()       != null) { total += summary.getAvgClarityRating();       parts++; }
        if (summary.getAvgDoubtClearingRating()  != null) { total += summary.getAvgDoubtClearingRating(); parts++; }
        if (summary.getAvgEnergyRating()         != null) { total += summary.getAvgEnergyRating();        parts++; }
        if (summary.getAvgTechnicalDepthRating() != null) { total += summary.getAvgTechnicalDepthRating();parts++; }
        summary.setOverallAvgRating(parts > 0 ? total / parts : null);

        summaryRepository.save(summary);

        try {
            eventProducer.publishSummaryUpdated(trainerEmail, batchId);
        } catch (Exception e) {
            log.warn("Kafka down during summary event: {}", e.getMessage());
        }

        log.info("Summary recomputed: trainer={} batch={} count={} overall={}",
                 trainerEmail, batchId, count, summary.getOverallAvgRating());
    }
    /**
     * ✅ NEW: Check if student already submitted feedback for a batch
     */
    @Transactional(readOnly = true)
    public boolean hasFeedback(String studentEmail, Long batchId) {
        return feedbackRepository.existsByStudentEmailAndBatchId(studentEmail, batchId);
    }

    private Feedback mapToEntity(SubmitFeedbackRequest req) {
        Feedback f = new Feedback();
        f.setBatchId(req.getBatchId());
        f.setStudentEmail(req.getStudentEmail());
        f.setTrainerEmail(req.getTrainerEmail());
        f.setMoodRating(Feedback.MoodRating.valueOf(req.getMoodRating().toUpperCase()));
        f.setTrainerClarityRating(req.getTrainerClarityRating());
        f.setTrainerDoubtClearingRating(req.getTrainerDoubtClearingRating());
        f.setTrainerEnergyRating(req.getTrainerEnergyRating());
        f.setTrainerTechnicalDepthRating(req.getTrainerTechnicalDepthRating());
        f.setContentTags(req.getContentTags());
        f.setImprovementTags(req.getImprovementTags());
        f.setComment(req.getComment());
        f.setAnonymous(req.isAnonymous());
        return f;
    }
}