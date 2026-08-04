package com.lms.live_session.service;
import com.lms.live_session.entity.AiUploadedResource;
import com.lms.live_session.entity.WhiteboardSnapshot;
import com.lms.live_session.repository.AiUploadedResourceRepository;
import com.lms.live_session.repository.WhiteboardSnapshotRepository;
import com.lms.live_session.dto.AiChatRequest;
import com.lms.live_session.entity.LiveSession;
import com.lms.live_session.entity.Recording;
import com.lms.live_session.repository.LiveSessionRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.lms.live_session.entity.ChatMessage;
import com.lms.live_session.repository.ChatMessageRepository;
@Service
public class AiContextBuilderService {

	private final LiveSessionRepository sessionRepository;
    private final RecordingService recordingService;
    private final WhiteboardSnapshotRepository whiteboardSnapshotRepository;
    private final WhiteboardTextExtractionService whiteboardTextExtractionService;
    private final AiUploadedResourceRepository uploadedResourceRepository;
    private final ChatMessageRepository chatMessageRepository;

    public AiContextBuilderService(
            LiveSessionRepository sessionRepository,
            RecordingService recordingService,
            WhiteboardSnapshotRepository whiteboardSnapshotRepository,
            WhiteboardTextExtractionService whiteboardTextExtractionService,
            AiUploadedResourceRepository uploadedResourceRepository,
            ChatMessageRepository chatMessageRepository) {
        this.sessionRepository = sessionRepository;
        this.recordingService = recordingService;
        this.whiteboardSnapshotRepository = whiteboardSnapshotRepository;
        this.whiteboardTextExtractionService = whiteboardTextExtractionService;
        this.uploadedResourceRepository = uploadedResourceRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    public String buildContext(AiChatRequest request) {
        List<String> contextParts = new ArrayList<>();
        List<String> sources = request.getSources();

        // ── MEETINGS source ────────────────────────────────────────────────────
        if (hasSources(sources, "MEETINGS", null)) {
            if (request.getSessionId() != null) {
                Optional<LiveSession> opt = sessionRepository.findById(request.getSessionId());
                opt.ifPresent(s -> {
                    StringBuilder sb = new StringBuilder("=== SESSION CONTEXT ===\n");
                    sb.append("Title: ").append(nvl(s.getTitle(), "N/A")).append("\n");
                    sb.append("Description: ").append(nvl(s.getDescription(), "Not provided")).append("\n");
                    sb.append("Batch ID: ").append(nvl(s.getBatchId(), "N/A")).append("\n");
                    sb.append("Duration: ").append(s.getDuration() != null ? s.getDuration() + " minutes" : "Unknown").append("\n");
                    sb.append("Status: ").append(nvl(s.getStatus(), "N/A")).append("\n");
                    if (s.getActualStartTime() != null) {
                        sb.append("Started at: ").append(s.getActualStartTime()).append("\n");
                    }
                    contextParts.add(sb.toString());
                });
            }
        }

     // ── CHAT source ────────────────────────────────────────────────────────
        if (hasSources(sources, "CHAT", null)) {
            contextParts.add(buildChatContext(request.getSessionId()));
        }

        // ── WHITEBOARD source ──────────────────────────────────────────────────
        if (hasSources(sources, "WHITEBOARD", null)) {
            contextParts.add(buildWhiteboardContext(request.getSessionId()));
        }

        // ── RECORDINGS source ──────────────────────────────────────────────────
        if (hasSources(sources, "RECORDINGS", null)) {
            contextParts.add(buildRecordingsContext(request.getSessionId()));
        }

        // ── DOCS / UPLOADS source ──────────────────────────────────────────────
     // ── DOCS / UPLOADS source ──────────────────────────────────────────────
        if (hasSources(sources, "DOCS", "UPLOADS")) {
            contextParts.add(buildDocsContext(request.getResourceIds()));
        }
        // ── Additional context from user ───────────────────────────────────────
        if (request.getAdditionalContext() != null && !request.getAdditionalContext().isBlank()) {
            contextParts.add("=== ADDITIONAL CONTEXT FROM USER ===\n" + request.getAdditionalContext() + "\n");
        }

        return contextParts.isEmpty() ? "" : "\n\n" + String.join("\n", contextParts);
    }

    /**
     * Builds the recordings context block for the AI prompt.
     * Uses real transcripts when available (Phase 2.3).
     */
    private String buildRecordingsContext(Long sessionId) {
        StringBuilder sb = new StringBuilder("=== RECORDINGS ===\n");

        if (sessionId == null) {
            sb.append("No session selected, so recordings cannot be loaded.\n");
            return sb.toString();
        }

        try {
            List<Recording> recordings = recordingService.getEntitiesBySession(sessionId);

            if (recordings == null || recordings.isEmpty()) {
                sb.append("No recordings found for this session.\n");
                return sb.toString();
            }

            sb.append("Total recordings found: ").append(recordings.size()).append("\n\n");

            for (int i = 0; i < recordings.size(); i++) {
                Recording r = recordings.get(i);
                sb.append("Recording ").append(i + 1).append(":\n");
                sb.append("  Title: ").append(nvl(r.getTitle(), "Untitled")).append("\n");
                sb.append("  Description: ").append(nvl(r.getDescription(), "Not provided")).append("\n");
                sb.append("  Status: ").append(nvl(r.getStatus(), "Unknown")).append("\n");
                sb.append("  Type: ").append(nvl(r.getRecordingType(), "Unknown")).append("\n");
                sb.append("  Duration: ").append(r.getDurationMinutes() != null ? r.getDurationMinutes() + " minutes" : "Unknown").append("\n");
                sb.append("  Uploaded At: ").append(r.getUploadedAt() != null ? r.getUploadedAt().toString() : "N/A").append("\n");

                String transcriptStatus = nvl(r.getTranscriptStatus(), "NOT_STARTED");
                sb.append("  Transcript Status: ").append(transcriptStatus).append("\n");

                if ("DONE".equals(transcriptStatus) && r.getTranscriptText() != null && !r.getTranscriptText().isBlank()) {
                    String transcript = r.getTranscriptText();
                    if (transcript.length() > 3000) {
                        transcript = transcript.substring(0, 3000) + "... [truncated]";
                    }
                    sb.append("  Transcript:\n  ").append(transcript.replace("\n", "\n  ")).append("\n");
                } else if ("PROCESSING".equals(transcriptStatus)) {
                    sb.append("  Transcript: still being generated, not available yet.\n");
                } else if ("FAILED".equals(transcriptStatus)) {
                    sb.append("  Transcript: generation failed for this recording.\n");
                } else {
                    sb.append("  Transcript: not available yet.\n");
                }
                sb.append("\n");
            }

        } catch (Exception e) {
            sb.append("Error loading recordings: ").append(e.getMessage()).append("\n");
        }

        return sb.toString();
    }

    /**
     * Builds the whiteboard context block for the AI prompt.
     * Extracts only text elements from the saved Excalidraw snapshot —
     * raw shape/stroke JSON is not sent (noisy + token-expensive).
     */
    private String buildWhiteboardContext(Long sessionId) {
        StringBuilder sb = new StringBuilder("=== WHITEBOARD ===\n");

        if (sessionId == null) {
            sb.append("No session selected, so whiteboard content cannot be loaded.\n");
            return sb.toString();
        }

        Optional<WhiteboardSnapshot> opt = whiteboardSnapshotRepository.findBySessionId(sessionId);
        if (opt.isEmpty()) {
            sb.append("No whiteboard content has been saved for this session.\n");
            return sb.toString();
        }

        String text = whiteboardTextExtractionService.extractText(opt.get().getElements());
        if (text.isBlank()) {
            sb.append("Whiteboard has content but no text elements were found (may contain only drawings/shapes).\n");
        } else {
            sb.append(text).append("\n");
        }

        return sb.toString();
    }

    /**
     * Lightweight session info loader for use by AiCompanionService
     * when full context builder is not needed.
     */
    public LiveSession loadSession(Long sessionId) {
        if (sessionId == null) return null;
        return sessionRepository.findById(sessionId).orElse(null);
    }

    /** Returns a list of sources that were actually used (for response metadata) */
    public List<String> getUsedSources(AiChatRequest request) {
        List<String> used = new ArrayList<>();
        if (request.getSources() == null) return used;
        for (String s : request.getSources()) {
            if (s != null && !s.isBlank()) used.add(s.toUpperCase());
        }
        return used;
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private boolean hasSources(List<String> sources, String s1, String s2) {
        if (sources == null || sources.isEmpty()) {
            return "MEETINGS".equals(s1);
        }
        for (String s : sources) {
            if (s1.equalsIgnoreCase(s)) return true;
            if (s2 != null && s2.equalsIgnoreCase(s)) return true;
        }
        return false;
    }

    private String nvl(Object val, String fallback) {
        return val == null ? fallback : val.toString();
    }
    /**
     * Builds the uploaded-documents context block for the AI prompt.
     * Appends extracted text per resource, truncated to keep prompts bounded.
     */
    private String buildDocsContext(List<Long> resourceIds) {
        StringBuilder sb = new StringBuilder("=== UPLOADED DOCUMENTS ===\n");

        if (resourceIds == null || resourceIds.isEmpty()) {
            sb.append("No documents were attached to this request.\n");
            return sb.toString();
        }

        List<AiUploadedResource> resources = uploadedResourceRepository.findAllById(resourceIds);
        if (resources.isEmpty()) {
            sb.append("No matching uploaded documents were found.\n");
            return sb.toString();
        }

        for (AiUploadedResource r : resources) {
            sb.append("Document: ").append(nvl(r.getFileName(), "Untitled")).append("\n");
            String text = r.getExtractedText();
            if (text == null || text.isBlank()) {
                sb.append("  [No extracted text available for this document]\n\n");
                continue;
            }
            if (text.length() > 3000) {
                text = text.substring(0, 3000) + "... [truncated]";
            }
            sb.append("  Content:\n  ").append(text.replace("\n", "\n  ")).append("\n\n");
        }

        return sb.toString();
    }
    /**
     * Builds the live-session chat context block for the AI prompt.
     */
    private String buildChatContext(Long sessionId) {
        StringBuilder sb = new StringBuilder("=== CHAT MESSAGES ===\n");

        if (sessionId == null) {
            sb.append("No session selected, so chat messages cannot be loaded.\n");
            return sb.toString();
        }

        List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderByTimestampAsc(sessionId);
        if (messages.isEmpty()) {
            sb.append("No chat messages found for this session.\n");
            return sb.toString();
        }

        for (ChatMessage m : messages) {
            String time = m.getTimestamp() != null ? m.getTimestamp().toString() : "unknown time";
            sb.append("[").append(time).append("] ")
              .append(nvl(m.getSenderRole(), "user"))
              .append(" (id=").append(m.getSenderId()).append("): ")
              .append(m.getMessage()).append("\n");
        }

        return sb.toString();
    }
}