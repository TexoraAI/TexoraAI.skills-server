//package com.lms.live_session.service;
//
//import com.lms.live_session.dto.AiChatRequest;
//import com.lms.live_session.entity.LiveSession;
//import com.lms.live_session.repository.LiveSessionRepository;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//
//@Service
//public class AiContextBuilderService {
//
//    private final LiveSessionRepository sessionRepository;
//
//    
//
//    public AiContextBuilderService(LiveSessionRepository sessionRepository) {
//        this.sessionRepository = sessionRepository;
//    }
//
//    
//    public String buildContext(AiChatRequest request) {
//        List<String> contextParts = new ArrayList<>();
//        List<String> sources = request.getSources();
//
//        // ── MEETINGS source ────────────────────────────────────────────────────
//        if (hasSources(sources, "MEETINGS", null)) {
//            if (request.getSessionId() != null) {
//                Optional<LiveSession> opt = sessionRepository.findById(request.getSessionId());
//                opt.ifPresent(s -> {
//                    StringBuilder sb = new StringBuilder("=== SESSION CONTEXT ===\n");
//                    sb.append("Title: ").append(nvl(s.getTitle(), "N/A")).append("\n");
//                    sb.append("Description: ").append(nvl(s.getDescription(), "Not provided")).append("\n");
//                    sb.append("Batch ID: ").append(nvl(s.getBatchId(), "N/A")).append("\n");
//                    sb.append("Duration: ").append(s.getDuration() != null ? s.getDuration() + " minutes" : "Unknown").append("\n");
//                    sb.append("Status: ").append(nvl(s.getStatus(), "N/A")).append("\n");
//                    if (s.getActualStartTime() != null) {
//                        sb.append("Started at: ").append(s.getActualStartTime()).append("\n");
//                    }
//                    contextParts.add(sb.toString());
//                });
//            }
//        }
//
//        // ── CHAT source ────────────────────────────────────────────────────────
//        if (hasSources(sources, "CHAT", null)) {
//            
//            contextParts.add("=== CHAT MESSAGES ===\n[TODO: Chat integration pending — no chat messages available yet]\n");
//        }
//
//        // ── WHITEBOARD source ──────────────────────────────────────────────────
//        if (hasSources(sources, "WHITEBOARD", null)) {
//            
//            contextParts.add("=== WHITEBOARD ===\n[TODO: Whiteboard snapshot integration pending]\n");
//        }
//
//        // ── RECORDINGS source ──────────────────────────────────────────────────
//        if (hasSources(sources, "RECORDINGS", null)) {
//            
//            contextParts.add("=== RECORDINGS ===\n[TODO: Recording transcript integration pending]\n");
//        }
//
//        // ── DOCS / UPLOADS source ──────────────────────────────────────────────
//        if (hasSources(sources, "DOCS", "UPLOADS")) {
//            if (request.getResourceIds() != null && !request.getResourceIds().isEmpty()) {
//                
//                contextParts.add("=== UPLOADED DOCUMENTS ===\n[TODO: Resource IDs received: " + request.getResourceIds() + " — file content integration pending]\n");
//            }
//        }
//
//        // ── Additional context from user ───────────────────────────────────────
//        if (request.getAdditionalContext() != null && !request.getAdditionalContext().isBlank()) {
//            contextParts.add("=== ADDITIONAL CONTEXT FROM USER ===\n" + request.getAdditionalContext() + "\n");
//        }
//
//        return contextParts.isEmpty() ? "" : "\n\n" + String.join("\n", contextParts);
//    }
//
//    /**
//     * Lightweight session info loader for use by AiCompanionService
//     * when full context builder is not needed.
//     */
//    public LiveSession loadSession(Long sessionId) {
//        if (sessionId == null) return null;
//        return sessionRepository.findById(sessionId).orElse(null);
//    }
//
//    /** Returns a list of sources that were actually used (for response metadata) */
//    public List<String> getUsedSources(AiChatRequest request) {
//        List<String> used = new ArrayList<>();
//        if (request.getSources() == null) return used;
//        for (String s : request.getSources()) {
//            if (s != null && !s.isBlank()) used.add(s.toUpperCase());
//        }
//        return used;
//    }
//
//    // ── Private helpers ────────────────────────────────────────────────────────
//
//    private boolean hasSources(List<String> sources, String s1, String s2) {
//        if (sources == null || sources.isEmpty()) {
//            // Default: include MEETINGS source when no sources specified
//            return "MEETINGS".equals(s1);
//        }
//        for (String s : sources) {
//            if (s1.equalsIgnoreCase(s)) return true;
//            if (s2 != null && s2.equalsIgnoreCase(s)) return true;
//        }
//        return false;
//    }
//
//    private String nvl(Object val, String fallback) {
//        return val == null ? fallback : val.toString();
//    }
//}

package com.lms.live_session.service;

import com.lms.live_session.dto.AiChatRequest;
import com.lms.live_session.dto.RecordingResponse;
import com.lms.live_session.entity.LiveSession;
import com.lms.live_session.repository.LiveSessionRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class AiContextBuilderService {

    private final LiveSessionRepository sessionRepository;
    private final RecordingService recordingService;

    public AiContextBuilderService(
            LiveSessionRepository sessionRepository,
            RecordingService recordingService) {
        this.sessionRepository = sessionRepository;
        this.recordingService = recordingService;
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
            contextParts.add("=== CHAT MESSAGES ===\n[TODO: Chat integration pending — no chat messages available yet]\n");
        }

        // ── WHITEBOARD source ──────────────────────────────────────────────────
        if (hasSources(sources, "WHITEBOARD", null)) {
            contextParts.add("=== WHITEBOARD ===\n[TODO: Whiteboard snapshot integration pending]\n");
        }

        // ── RECORDINGS source ──────────────────────────────────────────────────
        if (hasSources(sources, "RECORDINGS", null)) {
            contextParts.add(buildRecordingsContext(request.getSessionId()));
        }

        // ── DOCS / UPLOADS source ──────────────────────────────────────────────
        if (hasSources(sources, "DOCS", "UPLOADS")) {
            if (request.getResourceIds() != null && !request.getResourceIds().isEmpty()) {
                contextParts.add("=== UPLOADED DOCUMENTS ===\n[TODO: Resource IDs received: " + request.getResourceIds() + " — file content integration pending]\n");
            }
        }

        // ── Additional context from user ───────────────────────────────────────
        if (request.getAdditionalContext() != null && !request.getAdditionalContext().isBlank()) {
            contextParts.add("=== ADDITIONAL CONTEXT FROM USER ===\n" + request.getAdditionalContext() + "\n");
        }

        return contextParts.isEmpty() ? "" : "\n\n" + String.join("\n", contextParts);
    }

    /**
     * Builds the recordings context block for the AI prompt.
     */
    private String buildRecordingsContext(Long sessionId) {
        StringBuilder sb = new StringBuilder("=== RECORDINGS ===\n");

        if (sessionId == null) {
            sb.append("No session selected, so recordings cannot be loaded.\n");
            return sb.toString();
        }

        try {
            List<RecordingResponse> recordings = recordingService.getBySession(sessionId);

            if (recordings == null || recordings.isEmpty()) {
                sb.append("No recordings found for this session.\n");
                return sb.toString();
            }

            sb.append("Total recordings found: ").append(recordings.size()).append("\n\n");

            for (int i = 0; i < recordings.size(); i++) {
                RecordingResponse r = recordings.get(i);
                sb.append("Recording ").append(i + 1).append(":\n");
                sb.append("  Title: ").append(nvl(r.getTitle(), "Untitled")).append("\n");
                sb.append("  Description: ").append(nvl(r.getDescription(), "Not provided")).append("\n");
                sb.append("  Status: ").append(nvl(r.getStatus(), "Unknown")).append("\n");
                sb.append("  Type: ").append(nvl(r.getRecordingType(), "Unknown")).append("\n");
                sb.append("  Duration: ").append(r.getDurationMinutes() != null ? r.getDurationMinutes() + " minutes" : "Unknown").append("\n");
                sb.append("  File Name: ").append(nvl(r.getFileName(), "N/A")).append("\n");
                sb.append("  File Type: ").append(nvl(r.getFileType(), "N/A")).append("\n");
                sb.append("  File Size: ").append(nvl(r.getFileSizeMb(), "Unknown")).append("\n");
                sb.append("  File Path: ").append(nvl(r.getFilePath(), "N/A")).append("\n");
                sb.append("  Uploaded At: ").append(r.getUploadedAt() != null ? r.getUploadedAt().toString() : "N/A").append("\n");
                sb.append("  Created At: ").append(r.getCreatedAt() != null ? r.getCreatedAt().toString() : "N/A").append("\n");
            }

            sb.append("\nNote: Only recording metadata is available. Spoken audio/video transcript is not available yet.\n");

        } catch (Exception e) {
            sb.append("Error loading recordings: ").append(e.getMessage()).append("\n");
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
}