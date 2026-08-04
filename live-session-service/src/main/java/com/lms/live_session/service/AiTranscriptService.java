//package com.lms.live_session.service;
//
//import com.lms.live_session.dto.AiChatRequest;
//import com.lms.live_session.dto.AiChatResponse;
//import com.lms.live_session.dto.AiTranscriptAskRequest;
//import com.lms.live_session.dto.AiTranscriptSegmentRequest;
//import com.lms.live_session.dto.AiTranscriptStartRequest;
//import com.lms.live_session.entity.AiTranscriptSegment;
//import com.lms.live_session.entity.AiTranscriptSession;
//import com.lms.live_session.entity.AiTranscriptSession.TranscriptStatus;
//import com.lms.live_session.repository.AiTranscriptSegmentRepository;
//import com.lms.live_session.repository.AiTranscriptSessionRepository;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Service
//public class AiTranscriptService {
//
//    private final AiTranscriptSessionRepository sessionRepo;
//    private final AiTranscriptSegmentRepository segmentRepo;
//    private final AiCompanionService aiCompanionService;
//
//    public AiTranscriptService(
//        AiTranscriptSessionRepository sessionRepo,
//        AiTranscriptSegmentRepository segmentRepo,
//        AiCompanionService aiCompanionService
//    ) {
//        this.sessionRepo = sessionRepo;
//        this.segmentRepo = segmentRepo;
//        this.aiCompanionService = aiCompanionService;
//    }
//
//    // ── Start transcript session ───────────────────────────────────────────────
//    public AiTranscriptSession startSession(AiTranscriptStartRequest req, String trainerEmail) {
//        AiTranscriptSession session = new AiTranscriptSession();
//        session.setTrainerEmail(trainerEmail);
//        session.setLiveSessionId(req.getLiveSessionId());
//        session.setTitle(req.getTitle() != null ? req.getTitle() : "In-Person Notes");
//        session.setStatus(TranscriptStatus.RECORDING);
//        session.setStartedAt(LocalDateTime.now());
//        return sessionRepo.save(session);
//    }
//
//    // ── Add segment ────────────────────────────────────────────────────────────
//    public AiTranscriptSegment addSegment(Long transcriptId, AiTranscriptSegmentRequest req) {
//        AiTranscriptSession ts = sessionRepo.findById(transcriptId)
//            .orElseThrow(() -> new IllegalArgumentException("Transcript session not found: " + transcriptId));
//
//        AiTranscriptSegment seg = new AiTranscriptSegment();
//        seg.setTranscriptSessionId(ts.getId());
//        seg.setText(req.getText());
//        seg.setSpeakerName(req.getSpeakerName() != null ? req.getSpeakerName() : "Speaker 1");
//        seg.setStartedAtSecond(req.getStartedAtSecond() != null ? req.getStartedAtSecond() : 0);
//        return segmentRepo.save(seg);
//    }
//
//    // ── Stop transcript session ────────────────────────────────────────────────
//    public AiTranscriptSession stopSession(Long transcriptId) {
//        AiTranscriptSession ts = sessionRepo.findById(transcriptId)
//            .orElseThrow(() -> new IllegalArgumentException("Transcript session not found: " + transcriptId));
//        ts.setStatus(TranscriptStatus.COMPLETED);
//        ts.setStoppedAt(LocalDateTime.now());
//        return sessionRepo.save(ts);
//    }
//
//    // ── Get full transcript with segments ─────────────────────────────────────
//    public Map<String, Object> getTranscript(Long transcriptId) {
//        AiTranscriptSession ts = sessionRepo.findById(transcriptId)
//            .orElseThrow(() -> new IllegalArgumentException("Transcript session not found: " + transcriptId));
//        List<AiTranscriptSegment> segments = segmentRepo
//            .findByTranscriptSessionIdOrderByStartedAtSecondAsc(transcriptId);
//        return Map.of("session", ts, "segments", segments);
//    }
//
//    // ── Generate summary ───────────────────────────────────────────────────────
//    public String generateSummary(Long transcriptId) {
//        List<AiTranscriptSegment> segments = segmentRepo
//            .findByTranscriptSessionIdOrderByStartedAtSecondAsc(transcriptId);
//
//        if (segments.isEmpty()) {
//            return "No transcript content available to summarize.";
//        }
//
//        String fullText = segments.stream()
//            .map(s -> s.getSpeakerName() + ": " + s.getText())
//            .collect(Collectors.joining("\n"));
//
//        try {
//            AiChatRequest req = new AiChatRequest();
//            req.setMode("CUSTOM_QUESTION");
//            req.setMessage(
//                "You are a meeting notes assistant. Summarize this transcript into:\n" +
//                "1. KEY TOPICS DISCUSSED\n" +
//                "2. ACTION ITEMS\n" +
//                "3. IMPORTANT DECISIONS\n\n" +
//                "Transcript:\n" + fullText
//            );
//            AiChatResponse resp = aiCompanionService.processRequest(req);
//            return resp.isSuccess() ? resp.getResponse() : fallbackSummary(segments);
//        } catch (Exception e) {
//            return fallbackSummary(segments);
//        }
//    }
//
//    // ── Answer question about transcript ──────────────────────────────────────
//    public String askAboutTranscript(Long transcriptId, AiTranscriptAskRequest req) {
//        List<AiTranscriptSegment> segments = segmentRepo
//            .findByTranscriptSessionIdOrderByStartedAtSecondAsc(transcriptId);
//
//        if (segments.isEmpty()) {
//            return "No transcript content available to answer questions about.";
//        }
//
//        String fullText = segments.stream()
//            .map(s -> s.getSpeakerName() + ": " + s.getText())
//            .collect(Collectors.joining("\n"));
//
//        try {
//            AiChatRequest aiReq = new AiChatRequest();
//            aiReq.setMode("CUSTOM_QUESTION");
//            aiReq.setMessage(
//                "Based on the following transcript, answer this question:\n" +
//                "Question: " + req.getQuestion() + "\n\n" +
//                "Transcript:\n" + fullText
//            );
//            AiChatResponse resp = aiCompanionService.processRequest(aiReq);
//            return resp.isSuccess() ? resp.getResponse()
//                : "Unable to answer based on transcript at this time.";
//        } catch (Exception e) {
//            return "Unable to answer based on transcript at this time.";
//        }
//    }
//
//    // ── Fallback summary without AI ───────────────────────────────────────────
//    private String fallbackSummary(List<AiTranscriptSegment> segments) {
//        StringBuilder sb = new StringBuilder("TRANSCRIPT SUMMARY\n\n");
//        sb.append("Total segments: ").append(segments.size()).append("\n\n");
//        sb.append("FULL TRANSCRIPT:\n");
//        segments.forEach(s -> sb.append("• ").append(s.getText()).append("\n"));
//        return sb.toString();
//    }
//}
package com.lms.live_session.service;

import com.lms.live_session.dto.AiChatRequest;
import com.lms.live_session.dto.AiChatResponse;
import com.lms.live_session.dto.AiTranscriptAskRequest;
import com.lms.live_session.dto.AiTranscriptSegmentRequest;
import com.lms.live_session.dto.AiTranscriptStartRequest;
import com.lms.live_session.entity.AiTranscriptSegment;
import com.lms.live_session.entity.AiTranscriptSession;
import com.lms.live_session.entity.AiTranscriptSession.TranscriptStatus;
import com.lms.live_session.repository.AiTranscriptSegmentRepository;
import com.lms.live_session.repository.AiTranscriptSessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiTranscriptService {

    private final AiTranscriptSessionRepository sessionRepo;
    private final AiTranscriptSegmentRepository segmentRepo;
    private final AiCompanionService aiCompanionService;
    private final AiWorkflowExecutionService aiWorkflowExecutionService;

    public AiTranscriptService(
        AiTranscriptSessionRepository sessionRepo,
        AiTranscriptSegmentRepository segmentRepo,
        AiCompanionService aiCompanionService,
        AiWorkflowExecutionService aiWorkflowExecutionService
    ) {
        this.sessionRepo = sessionRepo;
        this.segmentRepo = segmentRepo;
        this.aiCompanionService = aiCompanionService;
        this.aiWorkflowExecutionService = aiWorkflowExecutionService;
    }

    // ── Start transcript session ───────────────────────────────────────────────
    public AiTranscriptSession startSession(AiTranscriptStartRequest req, String trainerEmail) {
        AiTranscriptSession session = new AiTranscriptSession();
        session.setTrainerEmail(trainerEmail);
        session.setLiveSessionId(req.getLiveSessionId());
        session.setTitle(req.getTitle() != null ? req.getTitle() : "In-Person Notes");
        session.setStatus(TranscriptStatus.RECORDING);
        session.setStartedAt(LocalDateTime.now());
        AiTranscriptSession saved = sessionRepo.save(session);

        // t4 auto-trigger: "Transcript created". Only meaningful when this
        // transcript is tied to a live session — standalone notes
        // (liveSessionId == null) have nothing to match workflows against.
        // Never allowed to break transcript creation if it fails.
        if (saved.getLiveSessionId() != null) {
            try {
                aiWorkflowExecutionService.fireTrigger(
                    AiWorkflowExecutionService.TRIGGER_TRANSCRIPT_CREATED,
                    saved.getLiveSessionId(),
                    trainerEmail
                );
            } catch (Exception e) {
                System.err.println("[AiTranscriptService] Failed to fire transcript-created trigger: " + e.getMessage());
            }
        }

        return saved;
    }

    // ── Add segment ────────────────────────────────────────────────────────────
    public AiTranscriptSegment addSegment(Long transcriptId, AiTranscriptSegmentRequest req) {
        AiTranscriptSession ts = sessionRepo.findById(transcriptId)
            .orElseThrow(() -> new IllegalArgumentException("Transcript session not found: " + transcriptId));

        AiTranscriptSegment seg = new AiTranscriptSegment();
        seg.setTranscriptSessionId(ts.getId());
        seg.setText(req.getText());
        seg.setSpeakerName(req.getSpeakerName() != null ? req.getSpeakerName() : "Speaker 1");
        seg.setStartedAtSecond(req.getStartedAtSecond() != null ? req.getStartedAtSecond() : 0);
        return segmentRepo.save(seg);
    }

    // ── Stop transcript session ────────────────────────────────────────────────
    public AiTranscriptSession stopSession(Long transcriptId) {
        AiTranscriptSession ts = sessionRepo.findById(transcriptId)
            .orElseThrow(() -> new IllegalArgumentException("Transcript session not found: " + transcriptId));
        ts.setStatus(TranscriptStatus.COMPLETED);
        ts.setStoppedAt(LocalDateTime.now());
        return sessionRepo.save(ts);
    }

    // ── Get full transcript with segments ─────────────────────────────────────
    public Map<String, Object> getTranscript(Long transcriptId) {
        AiTranscriptSession ts = sessionRepo.findById(transcriptId)
            .orElseThrow(() -> new IllegalArgumentException("Transcript session not found: " + transcriptId));
        List<AiTranscriptSegment> segments = segmentRepo
            .findByTranscriptSessionIdOrderByStartedAtSecondAsc(transcriptId);
        return Map.of("session", ts, "segments", segments);
    }

    // ── Generate summary ───────────────────────────────────────────────────────
    // CHANGED: now takes userEmail — AiCompanionService.processRequest()
    // requires (AiChatRequest, String userEmail) per Phase 1. This was a
    // pre-existing compile error unrelated to Phase 4; fixed while in this
    // file per the spec. NOTE: the controller call site for this method
    // (AiTranscriptController, not attached) must be updated to pass the
    // trainer's email through, or this will not compile.
    public String generateSummary(Long transcriptId, String userEmail) {
        List<AiTranscriptSegment> segments = segmentRepo
            .findByTranscriptSessionIdOrderByStartedAtSecondAsc(transcriptId);

        if (segments.isEmpty()) {
            return "No transcript content available to summarize.";
        }

        String fullText = segments.stream()
            .map(s -> s.getSpeakerName() + ": " + s.getText())
            .collect(Collectors.joining("\n"));

        try {
            AiChatRequest req = new AiChatRequest();
            req.setMode("CUSTOM_QUESTION");
            req.setMessage(
                "You are a meeting notes assistant. Summarize this transcript into:\n" +
                "1. KEY TOPICS DISCUSSED\n" +
                "2. ACTION ITEMS\n" +
                "3. IMPORTANT DECISIONS\n\n" +
                "Transcript:\n" + fullText
            );
            AiChatResponse resp = aiCompanionService.processRequest(req, userEmail);
            return resp.isSuccess() ? resp.getResponse() : fallbackSummary(segments);
        } catch (Exception e) {
            return fallbackSummary(segments);
        }
    }

    // ── Answer question about transcript ──────────────────────────────────────
    // CHANGED: same userEmail threading as generateSummary() above, for the
    // same pre-existing compile-error reason.
    public String askAboutTranscript(Long transcriptId, AiTranscriptAskRequest req, String userEmail) {
        List<AiTranscriptSegment> segments = segmentRepo
            .findByTranscriptSessionIdOrderByStartedAtSecondAsc(transcriptId);

        if (segments.isEmpty()) {
            return "No transcript content available to answer questions about.";
        }

        String fullText = segments.stream()
            .map(s -> s.getSpeakerName() + ": " + s.getText())
            .collect(Collectors.joining("\n"));

        try {
            AiChatRequest aiReq = new AiChatRequest();
            aiReq.setMode("CUSTOM_QUESTION");
            aiReq.setMessage(
                "Based on the following transcript, answer this question:\n" +
                "Question: " + req.getQuestion() + "\n\n" +
                "Transcript:\n" + fullText
            );
            AiChatResponse resp = aiCompanionService.processRequest(aiReq, userEmail);
            return resp.isSuccess() ? resp.getResponse()
                : "Unable to answer based on transcript at this time.";
        } catch (Exception e) {
            return "Unable to answer based on transcript at this time.";
        }
    }

    // ── Fallback summary without AI ───────────────────────────────────────────
    private String fallbackSummary(List<AiTranscriptSegment> segments) {
        StringBuilder sb = new StringBuilder("TRANSCRIPT SUMMARY\n\n");
        sb.append("Total segments: ").append(segments.size()).append("\n\n");
        sb.append("FULL TRANSCRIPT:\n");
        segments.forEach(s -> sb.append("• ").append(s.getText()).append("\n"));
        return sb.toString();
    }
}