package com.lms.live_session.controller;
import org.springframework.security.core.context.SecurityContextHolder;
import com.lms.live_session.entity.LiveSession;
import com.lms.live_session.service.LiveKitTokenService;
import com.lms.live_session.service.LiveSessionService;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.springframework.messaging.simp.SimpMessagingTemplate;
@RestController
@RequestMapping("/api/live-sessions")
public class LiveSessionController {

    private final LiveSessionService service;
    private final LiveKitTokenService tokenService;
    private final SimpMessagingTemplate messagingTemplate;

    public LiveSessionController(
            LiveSessionService service,
            LiveKitTokenService tokenService,SimpMessagingTemplate messagingTemplate) {
    	

        this.service = service;
        this.tokenService = tokenService;
        this.messagingTemplate=messagingTemplate;
    }

    // Create live session
    @PostMapping
    public LiveSession createSession(@RequestBody LiveSession session) {
        return service.createSession(session);
    }

    // Start session (existing API)
    @PostMapping("/{id}/start")
    public LiveSession startSession(@PathVariable Long id) {
        return service.startSession(id);
    }

    // End session
    @PostMapping("/{id}/end")
    public LiveSession endSession(@PathVariable Long id) {
        return service.endSession(id);
    }

    // Get sessions by batch
    @GetMapping("/batch/{batchId}")
    public List<LiveSession> getBatchSessions(@PathVariable Long batchId) {
        return service.getBatchSessions(batchId);
    }

    // ---------------------------------------------------------
    // NEW API → Trainer start live and receive LiveKit token
    // ---------------------------------------------------------

    @PostMapping("/{id}/start-live")
    public Map<String, String> startLiveSession(@PathVariable Long id) {

        // update session status
        service.startSession(id);

        // generate trainer token
        String token = tokenService.generateTrainerToken(id);

        Map<String, String> response = new HashMap<>();

        response.put("room", "session-" + id);
        response.put("token", token);

        return response;
    }

    // ---------------------------------------------------------
    // NEW API → Student join live session
    // ---------------------------------------------------------

    @GetMapping("/{id}/join")
    public Map<String, String> joinSession(
            @PathVariable Long id,
            @RequestParam Long studentId) {

        String token = tokenService.generateStudentToken(id, studentId);

        Map<String, String> response = new HashMap<>();

        response.put("room", "session-" + id);
        response.put("token", token);

        return response;
    }
    @GetMapping("/batch/{batchId}/live")
    public List<LiveSession> getLiveSessions(@PathVariable Long batchId) {
        return service.getLiveSessions(batchId);
    }
    @DeleteMapping("/{id}")
    public String deleteSession(@PathVariable Long id) {
        service.deleteSession(id);
        return "Session deleted successfully";
    }
//    @PostMapping("/call/start")
//    public Map<String, String> startCall(@RequestParam String trainerEmail) {
//
//        String room = "call-" + trainerEmail + "-" + System.currentTimeMillis();
//
//        String token = tokenService.generateCallToken("student", room);
//
//        Map<String, String> response = new HashMap<>();
//        response.put("room", room);
//        response.put("token", token);
//
//        return response;
//    }
    @PostMapping("/call/start")
    public Map<String, String> startCall(@RequestParam String trainerEmail) {
        String room = "call-" + trainerEmail + "-" + System.currentTimeMillis();

        // ✅ Use correct room name in token
        String token = tokenService.generateCallToken("student-caller", room);

        messagingTemplate.convertAndSend("/topic/calls/" + trainerEmail, room);

        Map<String, String> res = new HashMap<>();
        res.put("room", room);
        res.put("token", token);
        return res;
    }
    @GetMapping("/call/join")
    public Map<String, String> joinCall(@RequestParam String room) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        String token = tokenService.generateCallToken(email, room);

        Map<String, String> response = new HashMap<>();
        response.put("room", room);
        response.put("token", token);

        return response;
    }
    
}