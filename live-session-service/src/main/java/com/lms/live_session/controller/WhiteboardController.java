package com.lms.live_session.controller;

import com.lms.live_session.dto.WhiteboardEvent;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
// NO class-level @RequestMapping here intentionally —
// because @MessageMapping (WebSocket/STOMP) lives on this class too.
// A class-level prefix would corrupt the STOMP destination path.
// Each REST method declares its full path inline instead.
public class WhiteboardController {

    private final SimpMessagingTemplate messagingTemplate;

    // In-memory store: latest whiteboard state per session
    // Late-joiners call GET /state to load the current board
    private final ConcurrentHashMap<Long, WhiteboardEvent> latestState = new ConcurrentHashMap<>();

    public WhiteboardController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // ─── WebSocket: Real-time draw sync ───────────────────────────────────────
    // Frontend publishes to:   /app/whiteboard/{sessionId}
    // All subscribers receive: /topic/whiteboard/{sessionId}
    @MessageMapping("/whiteboard/{sessionId}")
    @SendTo("/topic/whiteboard/{sessionId}")
    public WhiteboardEvent syncWhiteboard(
            @DestinationVariable Long sessionId,
            WhiteboardEvent event) {
        event.setSessionId(sessionId);
        event.setTimestamp(LocalDateTime.now().toString());
        if ("FULL_STATE".equals(event.getEventType())) {
            latestState.put(sessionId, event);
        }
        return event;
    }

    // ─── REST: Get current whiteboard state (for late joiners) ───────────────
    @GetMapping("/api/v1/live-sessions/{sessionId}/whiteboard/state")
    public WhiteboardEvent getWhiteboardState(@PathVariable Long sessionId) {
        return latestState.getOrDefault(sessionId, new WhiteboardEvent());
    }

    // ─── REST: Save whiteboard snapshot ──────────────────────────────────────
    @PostMapping("/api/v1/live-sessions/{sessionId}/whiteboard/save")
    public Map<String, Object> saveWhiteboard(
            @PathVariable Long sessionId,
            @RequestBody WhiteboardEvent event) {
        event.setSessionId(sessionId);
        event.setTimestamp(LocalDateTime.now().toString());
        latestState.put(sessionId, event);
        return Map.of(
            "saved", true,
            "sessionId", sessionId,
            "timestamp", event.getTimestamp()
        );
    }

    // ─── REST: Clear whiteboard ───────────────────────────────────────────────
    @PostMapping("/api/v1/live-sessions/{sessionId}/whiteboard/clear")
    public Map<String, Object> clearWhiteboard(@PathVariable Long sessionId) {
        latestState.remove(sessionId);
        WhiteboardEvent clearEvent = new WhiteboardEvent();
        clearEvent.setSessionId(sessionId);
        clearEvent.setEventType("CLEAR");
        clearEvent.setTimestamp(LocalDateTime.now().toString());
        messagingTemplate.convertAndSend("/topic/whiteboard/" + sessionId, clearEvent);
        return Map.of("cleared", true, "sessionId", sessionId);
    }
}