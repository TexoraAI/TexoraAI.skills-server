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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
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
    private final OpenAiClientService openAiClientService;

    public AiTranscriptService(
        AiTranscriptSessionRepository sessionRepo,
        AiTranscriptSegmentRepository segmentRepo,
        AiCompanionService aiCompanionService,
        AiWorkflowExecutionService aiWorkflowExecutionService,
        OpenAiClientService openAiClientService
    ) {
        this.sessionRepo = sessionRepo;
        this.segmentRepo = segmentRepo;
        this.aiCompanionService = aiCompanionService;
        this.aiWorkflowExecutionService = aiWorkflowExecutionService;
        this.openAiClientService = openAiClientService;
    }

    // ── Ownership check ─────────────────────────────────────────────────────────
    // NOTE: I don't have AiChatController/AiCompanionService.processRequest() or
    // WhiteboardController.checkAccess() in front of me, so I can't literally
    // mirror their exception type. ResponseStatusException is Spring MVC's
    // built-in mechanism for exactly this — it's resolved automatically to the
    // given status (404/403) without needing a custom @ExceptionHandler, so it
    // satisfies "proper status codes, not a generic 500" regardless of what
    // exception class those other two use internally. If the codebase already
    // has a shared AccessDeniedException/NotFoundException convention, swap
    // these two throws for that instead so the pattern is consistent everywhere.
    private AiTranscriptSession verifyOwnership(Long transcriptId, String callerEmail) {
        AiTranscriptSession ts = sessionRepo.findById(transcriptId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Transcript session not found: " + transcriptId));

        String owner = ts.getTrainerEmail();
        if (owner == null || callerEmail == null || !owner.equalsIgnoreCase(callerEmail)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, "You do not have access to transcript session: " + transcriptId);
        }
        return ts;
    }

    // ── Start transcript session ───────────────────────────────────────────────
    // No ownership check — this creates a new resource owned by the caller.
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
    // ── Add segment (manual/legacy text path — kept for compatibility) ─────────
    // CHANGED — now requires callerEmail; ownership verified before any write.
    public AiTranscriptSegment addSegment(Long transcriptId, AiTranscriptSegmentRequest req, String callerEmail) {
        AiTranscriptSession ts = verifyOwnership(transcriptId, callerEmail);

        AiTranscriptSegment seg = new AiTranscriptSegment();
        seg.setTranscriptSessionId(ts.getId());
        seg.setText(req.getText());
        seg.setSpeakerName(req.getSpeakerName() != null ? req.getSpeakerName() : "Speaker 1");
        seg.setStartedAtSecond(req.getStartedAtSecond() != null ? req.getStartedAtSecond() : 0);
        return segmentRepo.save(seg);
    }

    // ── Add segment from an uploaded audio chunk (Whisper — authoritative) ────
    // Called once per rolling MediaRecorder chunk from the frontend. This is
    // the ONLY path that writes authoritative transcript content now — Web
    // Speech results are captions-only and never reach this service. On
    // transcription failure this throws (mapped to 502 by the controller);
    // the caller (frontend) owns the retry-once + failed-in-UI behavior, so
    // nothing partial is persisted here on failure.
    public AiTranscriptSegment addAudioChunkSegment(
            Long transcriptId,
            MultipartFile audioFile,
            Integer chunkIndex,
            Integer startedAtSecond,
            String language,
            String callerEmail) {

        AiTranscriptSession ts = verifyOwnership(transcriptId, callerEmail);

        byte[] audioBytes;
        try {
            audioBytes = audioFile.getBytes();
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to read audio chunk: " + e.getMessage());
        }

        String originalName = audioFile.getOriginalFilename();

        String filename = "chunk_" +
                (chunkIndex != null ? chunkIndex : 0) +
                (originalName != null && originalName.contains(".")
                        ? originalName.substring(originalName.lastIndexOf("."))
                        : ".webm");

        String transcript;

        try {
            transcript = openAiClientService.transcribeAudio(
                    audioBytes,
                    filename,
                    language
            );
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Transcription failed for chunk "
                            + (chunkIndex != null ? chunkIndex : 0)
                            + ": "
                            + e.getMessage());
        }

        // Silence or no speech detected
        if (transcript == null || transcript.isBlank()) {
            return null;
        }

        AiTranscriptSegment seg = new AiTranscriptSegment();
        seg.setTranscriptSessionId(ts.getId());
        seg.setText(transcript.trim());
        seg.setSpeakerName("Speaker 1");
        seg.setStartedAtSecond(startedAtSecond != null ? startedAtSecond : 0);
        seg.setChunkIndex(chunkIndex);
        seg.setLanguage(language);

        return segmentRepo.save(seg);
    }
    // ── Stop transcript session ────────────────────────────────────────────────
    // CHANGED — now requires callerEmail; ownership verified before mutating status.
    public AiTranscriptSession stopSession(Long transcriptId, String callerEmail) {
        AiTranscriptSession ts = verifyOwnership(transcriptId, callerEmail);
        ts.setStatus(TranscriptStatus.COMPLETED);
        ts.setStoppedAt(LocalDateTime.now());
        return sessionRepo.save(ts);
    }

    // ── Get full transcript with segments ─────────────────────────────────────
    // CHANGED — now requires callerEmail; ownership verified before reading.
    public Map<String, Object> getTranscript(Long transcriptId, String callerEmail) {
        AiTranscriptSession ts = verifyOwnership(transcriptId, callerEmail);
        List<AiTranscriptSegment> segments = segmentRepo
            .findByTranscriptSessionIdOrderByStartedAtSecondAsc(transcriptId);
        return Map.of("session", ts, "segments", segments);
    }

    // ── Get transcript by live session id (virtual meetings) ──────────────────
    // Used by the Meetings page "View Transcript" button to open the
    // Whisper-derived transcript once AiTranscriptLinkService has linked it.
    public Map<String, Object> getTranscriptByLiveSessionId(Long liveSessionId, String callerEmail) {
        AiTranscriptSession ts = sessionRepo.findFirstByLiveSessionIdOrderByStartedAtDesc(liveSessionId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "No transcript found for live session: " + liveSessionId));

        String owner = ts.getTrainerEmail();
        if (owner == null || callerEmail == null || !owner.equalsIgnoreCase(callerEmail)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, "You do not have access to this transcript.");
        }

        List<AiTranscriptSegment> segments = segmentRepo
            .findByTranscriptSessionIdOrderByStartedAtSecondAsc(ts.getId());
        return Map.of("session", ts, "segments", segments);
    }

    // ── Generate summary ───────────────────────────────────────────────────────
    // CHANGED — ownership now verified before doing any work. Deliberately
    // called OUTSIDE the try/catch below: the catch exists to fall back to a
    // non-AI summary if the OpenAI call fails, and must never swallow a 403/404
    // and silently return 200 with fallback text instead.
    public String generateSummary(Long transcriptId, String userEmail) {
        verifyOwnership(transcriptId, userEmail);

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
            req.setMode("IN_PERSON_TRANSCRIPT_SUMMARY");
            req.setAdditionalContext("Transcript:\n" + truncateForContext(fullText));
            AiChatResponse resp = aiCompanionService.processRequest(req, userEmail, null);
            return resp.isSuccess() ? resp.getResponse() : fallbackSummary(segments);
        } catch (Exception e) {
            return fallbackSummary(segments);
        }
    }

    // ── Answer question about transcript ──────────────────────────────────────
    // CHANGED — same verifyOwnership-before-try reasoning as generateSummary()
    // above: a 403/404 must propagate, not get swallowed into a 200 response.
    public String askAboutTranscript(Long transcriptId, AiTranscriptAskRequest req, String userEmail) {
        verifyOwnership(transcriptId, userEmail);

        List<AiTranscriptSegment> segments = segmentRepo
            .findByTranscriptSessionIdOrderByStartedAtSecondAsc(transcriptId);

        if (segments.isEmpty()) {
            return "No transcript content available to answer questions about.";
        }

        String fullText = segments.stream()
            .map(s -> s.getSpeakerName() + ": " + s.getText())
            .collect(Collectors.joining("\n"));

//        try {
//            AiChatRequest aiReq = new AiChatRequest();
//            aiReq.setMode("CUSTOM_QUESTION");
//            aiReq.setMessage(
//                "Based on the following transcript, answer this question:\n" +
//                "Question: " + req.getQuestion() + "\n\n" +
//                "Transcript:\n" + fullText
//            );
//            AiChatResponse resp = aiCompanionService.processRequest(aiReq, userEmail, null);
//            return resp.isSuccess() ? resp.getResponse()
//                : "Unable to answer based on transcript at this time.";
//        } catch (Exception e) {
//            return "Unable to answer based on transcript at this time.";
//        }
        try {
            AiChatRequest aiReq = new AiChatRequest();
            aiReq.setMode("IN_PERSON_TRANSCRIPT_QA");
            aiReq.setMessage(req.getQuestion());
            aiReq.setAdditionalContext("Transcript:\n" + truncateForContext(fullText));
            AiChatResponse resp = aiCompanionService.processRequest(aiReq, userEmail, null);
            return resp.isSuccess() ? resp.getResponse()
                : "Unable to answer based on transcript at this time.";
        } catch (Exception e) {
            return "Unable to answer based on transcript at this time.";
        }
    }

    // ── Context truncation ─────────────────────────────────────────────────────
    // Same cap-and-notice pattern as AiContextBuilderService's
    // buildRecordingsContext/buildDocsContext (3000 chars + truncation notice).
    // Previously fullText was concatenated into the prompt with no limit.
    private static final int MAX_TRANSCRIPT_CONTEXT_CHARS = 3000;

    private String truncateForContext(String text) {
        if (text.length() <= MAX_TRANSCRIPT_CONTEXT_CHARS) {
            return text;
        }
        return text.substring(0, MAX_TRANSCRIPT_CONTEXT_CHARS) + "... [truncated]";
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