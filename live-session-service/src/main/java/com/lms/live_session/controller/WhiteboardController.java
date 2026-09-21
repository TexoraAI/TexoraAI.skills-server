package com.lms.live_session.controller;
import org.springframework.security.core.Authentication;
import com.lms.live_session.dto.WhiteboardEvent;
import com.lms.live_session.entity.LiveSession;
import com.lms.live_session.repository.LiveSessionRepository;
import com.lms.live_session.security.JwtUtil;
import com.lms.live_session.websocket.WhiteboardAuthChannelInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.lms.live_session.entity.WhiteboardSnapshot;
import com.lms.live_session.repository.WhiteboardSnapshotRepository;
@RestController
// NO class-level @RequestMapping here intentionally —
// because @MessageMapping (WebSocket/STOMP) lives on this class too.
// A class-level prefix would corrupt the STOMP destination path.
// Each REST method declares its full path inline instead.
public class WhiteboardController {

    private static final Logger log = LoggerFactory.getLogger(WhiteboardController.class);


	private final SimpMessagingTemplate messagingTemplate;
    private final WhiteboardSnapshotRepository snapshotRepository;
    private final LiveSessionRepository liveSessionRepository;
    private final JwtUtil jwtUtil;
    private final com.lms.live_session.service.LiveSessionUsageService usageService;

    // In-memory store: latest whiteboard state per session
    // Late-joiners call GET /state to load the current board
    private final ConcurrentHashMap<Long, WhiteboardEvent> latestState = new ConcurrentHashMap<>();

    public WhiteboardController(SimpMessagingTemplate messagingTemplate,
                                 WhiteboardSnapshotRepository snapshotRepository,
                                 LiveSessionRepository liveSessionRepository,
                                 JwtUtil jwtUtil,
                                 com.lms.live_session.service.LiveSessionUsageService usageService) {
        this.messagingTemplate = messagingTemplate;
        this.snapshotRepository = snapshotRepository;
        this.liveSessionRepository = liveSessionRepository;
        this.jwtUtil = jwtUtil;
        this.usageService = usageService;
    }
    // Upserts the DB snapshot (additive — does not touch the in-memory map)
    private void persistSnapshot(Long sessionId, WhiteboardEvent event) {
        WhiteboardSnapshot snapshot = snapshotRepository.findBySessionId(sessionId)
                .orElseGet(WhiteboardSnapshot::new);
        snapshot.setSessionId(sessionId);
        snapshot.setElements(event.getElements());
        snapshot.setAppState(event.getAppState());
        snapshot.setFiles(event.getFiles());
        snapshotRepository.save(snapshot);
    }

    // ── Ownership check — shared by all REST endpoints below.
    // Trainer-only: the whiteboard belongs to whichever trainer created the
    // session (session.getTrainerEmail()), regardless of that trainer's
    // organizationId (org A trainer, or a non-org trainer — either way,
    // ownership is decided purely by email match against the session).
    //   - Session not found -> 404.
    //   - Caller email doesn't match session.getTrainerEmail() -> 403.
    //   - Otherwise -> allowed.
    // NOTE: this only covers the REST endpoints. The @MessageMapping
    // WebSocket endpoint has its own check below (isWebSocketSessionOwner) —
    // it can't reuse this method as-is because there's no
    // HttpServletRequest on a STOMP frame, and throwing a
    // ResponseStatusException wouldn't mean anything to a WebSocket client.
    private LiveSession checkAccess(Long sessionId, HttpServletRequest httpRequest) {
        LiveSession session = liveSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Session not found: " + sessionId));

        String rawToken = null;
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            rawToken = authHeader.substring(7);
        }

        String callerEmail = rawToken != null ? jwtUtil.extractEmail(rawToken) : null;

        boolean isTrainerOwner = callerEmail != null
                && session.getTrainerEmail() != null
                && session.getTrainerEmail().equalsIgnoreCase(callerEmail);

        if (!isTrainerOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access denied: you are not authorized to access this session's whiteboard.");
        }

        return session;
    }

    // ── Ownership check for the WebSocket path — trainer-only.
    // The caller's email comes from WhiteboardAuthChannelInterceptor, which
    // validates the JWT once at STOMP CONNECT time and stashes the email in
    // the STOMP session attributes (see that class for details). Here we
    // just read it back and compare against the session's trainerEmail,
    // same rule as checkAccess() above, minus the HTTP-specific plumbing.
    // Returns false (never throws) so syncWhiteboard() can log-and-drop
    // instead of blowing up the STOMP session over a bad publish.
    private boolean isWebSocketSessionOwner(Long sessionId, String callerEmail) {
        if (callerEmail == null || callerEmail.isBlank()) {
            return false;
        }
        return liveSessionRepository.findById(sessionId)
                .map(session -> session.getTrainerEmail() != null
                        && session.getTrainerEmail().equalsIgnoreCase(callerEmail))
                .orElse(false);
    }

    // ─── WebSocket: Real-time draw sync ───────────────────────────────────────
    // Frontend publishes to:   /app/whiteboard/{sessionId}
    // All subscribers receive: /topic/whiteboard/{sessionId}
    //
    // Trainer-broadcast-only whiteboard: students may SUBSCRIBE to receive
    // broadcasts (read-only viewing — untouched by this method), but any
    // SEND from a caller who isn't the session's trainer is rejected here
    // and never reaches the broadcast or the DB.
    //
    // callerEmail is populated by WhiteboardAuthChannelInterceptor at
    // STOMP CONNECT time (see that class) and read back via
    // SimpMessageHeaderAccessor#getSessionAttributes(). If the interceptor
    // never ran (e.g. an unauthenticated CONNECT that should already have
    // been rejected upstream) this comes back null and the caller is
    // treated as unauthorized.
    @MessageMapping("/whiteboard/{sessionId}")
    @SendTo("/topic/whiteboard/{sessionId}")
    public WhiteboardEvent syncWhiteboard(
            @DestinationVariable Long sessionId,
            WhiteboardEvent event,
            SimpMessageHeaderAccessor headerAccessor) {

        String callerEmail = headerAccessor.getSessionAttributes() != null
                ? (String) headerAccessor.getSessionAttributes().get(WhiteboardAuthChannelInterceptor.SESSION_ATTR_EMAIL)
                : null;

        if (!isWebSocketSessionOwner(sessionId, callerEmail)) {
            log.warn("Rejected whiteboard publish: sessionId={}, callerEmail={}, eventType={} — " +
                            "caller is not this session's trainer (trainer-only whiteboard).",
                    sessionId, callerEmail, event.getEventType());
            // Returning null here means @SendTo sends nothing: no broadcast
            // to /topic/whiteboard/{sessionId}, and we fall out before the
            // FULL_STATE persist path below ever runs.
            return null;
        }


        event.setSessionId(sessionId);
        event.setTimestamp(LocalDateTime.now().toString());
        if ("FULL_STATE".equals(event.getEventType())) {
            latestState.put(sessionId, event);
            persistSnapshot(sessionId, event);
        }
        return event;
    }

    // ─── REST: Get current whiteboard state (for late joiners) ───────────────
    @GetMapping("/api/v1/live-sessions/{sessionId}/whiteboard/state")
    public WhiteboardEvent getWhiteboardState(
            @PathVariable Long sessionId,
            HttpServletRequest httpRequest) {
        checkAccess(sessionId, httpRequest);
        return latestState.getOrDefault(sessionId, new WhiteboardEvent());
    }

    // ─── REST: Save whiteboard snapshot ──────────────────────────────────────
    // ─── REST: Save whiteboard snapshot ──────────────────────────────────────
    @PostMapping("/api/v1/live-sessions/{sessionId}/whiteboard/save")
    public Map<String, Object> saveWhiteboard(
            @PathVariable Long sessionId,
            @RequestBody WhiteboardEvent event,
            HttpServletRequest httpRequest) {
        LiveSession session = checkAccess(sessionId, httpRequest);
        usageService.checkWhiteboardAccess(session.getOrganizationId(), session.getTrainerEmail());

    	event.setSessionId(sessionId);
        event.setTimestamp(LocalDateTime.now().toString());
        latestState.put(sessionId, event);
        persistSnapshot(sessionId, event);
        return Map.of(
            "saved", true,
            "sessionId", sessionId,
            "timestamp", event.getTimestamp()
        );
    }

    // ─── REST: Whiteboard access status (for the frontend badge) ─────────────
    // Resolves tier the SAME way saveWhiteboard does — via this specific
    // session's stamped organizationId — so the badge never disagrees with
    // what save() will actually allow. Deliberately does NOT reuse the
    // JWT-based class-usage endpoint's tier, which can differ when the
    // caller's JWT org and this session's stored org don't match.
    @GetMapping("/api/v1/live-sessions/{sessionId}/whiteboard/access")
    public Map<String, Object> getWhiteboardAccess(
            @PathVariable Long sessionId,
            HttpServletRequest httpRequest) {
        LiveSession session = checkAccess(sessionId, httpRequest);
        return usageService.getWhiteboardAccessStatus(session.getOrganizationId(), session.getTrainerEmail());
    }

    // ─── REST: Standalone whiteboard access check (no live session) ─────────
    // For whiteboards created outside a class (dashboard "New Whiteboard"),
    // there's no LiveSession row to derive organizationId/trainerEmail from,
    // so this resolves both directly from the caller's JWT instead. Used by
    // both the badge AND as a real save-time gate — standalone saves must
    // call this and block on !available, the same way saveWhiteboard()
    // blocks via usageService.checkWhiteboardAccess().
    //
    // Uses the Authentication object Spring Security/JwtFilter already
    // populated (same pattern as every other controller — see
    // MeetingController.getMeetingUsage) instead of re-parsing the raw
    // header, so this can never disagree with what JwtFilter already
    // validated.
    @GetMapping("/api/v1/live-sessions/whiteboard/access-standalone")
    public Map<String, Object> getStandaloneWhiteboardAccess(
            Authentication auth, HttpServletRequest httpRequest) {
        if (auth == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }
        String callerEmail = auth.getName();
        String rawToken = extractRawToken(httpRequest);
        Long organizationId = resolveOrganizationId(rawToken);
        return usageService.getWhiteboardAccessStatus(organizationId, callerEmail);
    }

    // ─── REST: Standalone whiteboard save-time gate (no live session) ───────
    @PostMapping("/api/v1/live-sessions/whiteboard/check-standalone-save")
    public Map<String, Object> checkStandaloneWhiteboardSave(
            Authentication auth, HttpServletRequest httpRequest) {
        if (auth == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }
        String callerEmail = auth.getName();
        String rawToken = extractRawToken(httpRequest);
        Long organizationId = resolveOrganizationId(rawToken);
        usageService.checkWhiteboardAccess(organizationId, callerEmail); // throws if not available
        return Map.of("allowed", true);
    }

    private String extractRawToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private Long resolveOrganizationId(String token) {
        if (token == null) return null;
        String orgIdStr = jwtUtil.extractOrganizationId(token);
        return orgIdStr != null ? Long.parseLong(orgIdStr) : null;
    }
    // ─── REST: Clear whiteboard ───────────────────────────────────────────────
    @PostMapping("/api/v1/live-sessions/{sessionId}/whiteboard/clear")
    public Map<String, Object> clearWhiteboard(
            @PathVariable Long sessionId,
            HttpServletRequest httpRequest) {
        checkAccess(sessionId, httpRequest);
        latestState.remove(sessionId);
        WhiteboardEvent clearEvent = new WhiteboardEvent();
        clearEvent.setSessionId(sessionId);
        clearEvent.setEventType("CLEAR");
        clearEvent.setTimestamp(LocalDateTime.now().toString());
        messagingTemplate.convertAndSend("/topic/whiteboard/" + sessionId, clearEvent);
        return Map.of("cleared", true, "sessionId", sessionId);
    }
}