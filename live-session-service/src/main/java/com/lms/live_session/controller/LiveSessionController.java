
package com.lms.live_session.controller;

import com.lms.live_session.dto.PublicBookingRequest;

import com.lms.live_session.dto.PublicBookingResponse;
import com.lms.live_session.dto.RecordingResponse;
import com.lms.live_session.entity.LiveSession;
import com.lms.live_session.entity.PublicSessionBooking;
import com.lms.live_session.entity.SessionParticipant;
import com.lms.live_session.exception.LiveSessionAccessDeniedException;
import com.lms.live_session.security.LiveSessionAccessValidator;
import com.lms.live_session.security.JwtUtil;
import com.lms.live_session.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/live-sessions")
public class LiveSessionController {

    private final LiveSessionService service;
    private final LiveKitTokenService tokenService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ParticipantService participantService;
    private final PublicBookingService bookingService;
    private final UrlBuilderService urlBuilderService;
    private final RecordingService recordingService;
    private final LiveSessionAccessValidator accessValidator;
    private final JwtUtil jwtUtil;
    private final LiveSessionUsageService usageService;

    public LiveSessionController(
            LiveSessionService service,
            LiveKitTokenService tokenService,
            SimpMessagingTemplate messagingTemplate,
            ParticipantService participantService,
            PublicBookingService bookingService,
            UrlBuilderService urlBuilderService,
            RecordingService recordingService,
            LiveSessionAccessValidator accessValidator,
            JwtUtil jwtUtil,
            LiveSessionUsageService usageService) {
        this.service           = service;
        this.tokenService      = tokenService;
        this.messagingTemplate = messagingTemplate;
        this.participantService = participantService;
        this.bookingService    = bookingService;
        this.urlBuilderService = urlBuilderService;
        this.recordingService  = recordingService;
        this.accessValidator   = accessValidator;
        this.jwtUtil            = jwtUtil;
        this.usageService       = usageService;
    }
    // ═══════════════════════════════════════════════════════
    // LIVE SESSION CRUD
    // ═══════════════════════════════════════════════════════

//    @PostMapping
//    public ResponseEntity<?> createSession(
//            @RequestBody LiveSession session,
//            Authentication auth,
//            HttpServletRequest request) {
//        try {
//            session.setTrainerEmail(auth.getName());
//
//            String token = extractRawToken(request);
//            Long organizationId = accessValidator.validateAndResolveOrganizationId(
//                token, session.getBatchId());
//            session.setOrganizationId(organizationId);
//
//            LiveSession created = service.createSession(session);
//            return ResponseEntity.ok(created);
//        } catch (LiveSessionAccessDeniedException e) {
//            return ResponseEntity.badRequest().body(new ErrorResponse("Access denied: " + e.getMessage()));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to create session: " + e.getMessage()));
//        }
//    }
    
    @PostMapping
    public ResponseEntity<?> createSession(
            @RequestBody LiveSession session,
            Authentication auth,
            HttpServletRequest request) {
        try {
            session.setTrainerEmail(auth.getName());

            String token = extractRawToken(request);
            Long organizationId = accessValidator.validateAndResolveOrganizationId(
                token, session.getBatchId());
            session.setOrganizationId(organizationId);

            LiveSession created = service.createSession(session);
            return ResponseEntity.ok(created);
        } catch (LiveSessionAccessDeniedException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Access denied: " + e.getMessage()));
        } catch (com.lms.live_session.exception.LiveClassLimitExceededException e) {
            throw e; // let GlobalExceptionHandler produce 429 + CLASS_LIMIT_EXCEEDED
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to create session: " + e.getMessage()));
        }
    }

    private String extractRawToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    // ✅ NEW — resolves the CALLER's own org id for read-side filtering.
    // Unlike accessValidator (which checks trainer-vs-batch org match for
    // writes), this is a simple "what org is this caller in" lookup — no
    // mismatch is possible on a pure read, so no exception path needed.
    private Long resolveCallerOrgId(HttpServletRequest request) {
        String token = extractRawToken(request);
        if (token == null) return null;
        String orgIdStr = jwtUtil.extractOrganizationId(token);
        if (orgIdStr == null) return null;
        try {
            return Long.parseLong(orgIdStr);
        } catch (NumberFormatException e) {
            return null; // malformed claim on a read path — fail open to unrestricted rather than 500
        }
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<?> startSession(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.startSession(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/{id}/end")
    public ResponseEntity<?> endSession(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.endSession(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSession(@PathVariable Long id) {
        try {
            service.deleteSession(id);
            return ResponseEntity.ok(Map.of("message", "Session deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSessionById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.getSessionById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/history")
    public List<LiveSession> getSessionHistory(Authentication auth) {
        return service.getMySessionsAsTrainer(auth.getName());
    }

    @GetMapping("/batch/{batchId}")
    public List<LiveSession> getBatchSessions(@PathVariable Long batchId, HttpServletRequest request) {
        return service.getBatchSessions(batchId, resolveCallerOrgId(request));
    }

    @GetMapping("/batch/{batchId}/live")
    public List<LiveSession> getLiveSessions(@PathVariable Long batchId, HttpServletRequest request) {
        return service.getLiveSessions(batchId, resolveCallerOrgId(request));
    }

    @GetMapping("/{id}/meeting-link")
    public ResponseEntity<?> getMeetingLink(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.resolveMeetingLink(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/calendar")
    public ResponseEntity<List<LiveSession>> getCalendar(
            @RequestParam String from,
            @RequestParam String to,
            Authentication auth) {
        java.time.LocalDate fromDate = java.time.LocalDate.parse(from);
        java.time.LocalDate toDate   = java.time.LocalDate.parse(to);
        return ResponseEntity.ok(
            service.getTrainerCalendar(auth.getName(), fromDate, toDate));
    }

    @GetMapping("/published")
    public ResponseEntity<List<LiveSession>> getPublishedSessions() {
        return ResponseEntity.ok(service.getPublishedSessions());
    }

    // ═══════════════════════════════════════════════════════
    // USAGE PREVIEW (ungated)
    // ═══════════════════════════════════════════════════════

    @GetMapping("/usage/classes")
    public ResponseEntity<?> getClassUsage(Authentication auth, HttpServletRequest request) {
        Long orgId = resolveCallerOrgId(request);
        return ResponseEntity.ok(usageService.getUsageStatus(orgId, auth.getName(), "CLASS_CREATE"));
    }

    @GetMapping("/usage/ai-companion")
    public ResponseEntity<?> getAiCompanionUsage(Authentication auth, HttpServletRequest request) {
        Long orgId = resolveCallerOrgId(request);
        return ResponseEntity.ok(usageService.getUsageStatus(orgId, auth.getName(), "AI_COMPANION_USE"));
    }

    // ═══════════════════════════════════════════════════════
    // CAN-START CHECK
    // ═══════════════════════════════════════════════════════
    // ═══════════════════════════════════════════════════════
    // CAN-START CHECK
    // ═══════════════════════════════════════════════════════

    @GetMapping("/{id}/can-start")
    public ResponseEntity<Map<String, Object>> canStartSession(@PathVariable Long id) {
        try {
            LiveSession session = service.getSessionById(id);
            Map<String, Object> result = new HashMap<>();

            boolean canStart = service.canStart(session);
            result.put("canStart", canStart);
            result.put("status", session.getStatus());
            result.put("createdAt", session.getCreatedAt() != null ? session.getCreatedAt().toString() : null);

            if (session.getScheduledDate() != null && session.getScheduledTime() != null) {
                LocalDateTime scheduledAt = LocalDateTime.of(
                    session.getScheduledDate(), session.getScheduledTime()
                );
                long diffMinutes = ChronoUnit.MINUTES.between(LocalDateTime.now(), scheduledAt);
                result.put("scheduledAt", scheduledAt.toString());
                result.put("minutesUntilStart", diffMinutes);
                result.put("reason", canStart
                    ? "Within start window"
                    : diffMinutes > 15
                        ? "Too early — opens " + diffMinutes + " min before start"
                        : "Session not in SCHEDULED state");
            } else {
                result.put("reason", canStart ? "No schedule set — allowed" : "Not SCHEDULED");
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("canStart", false, "reason", e.getMessage()));
        }
    }

    // ═══════════════════════════════════════════════════════
    // LIVEKIT TOKENS
    // ═══════════════════════════════════════════════════════

    @PostMapping("/{id}/start-live")
    public ResponseEntity<?> startLiveSession(@PathVariable Long id, Authentication auth) {
        try {
            LiveSession session = service.getSessionById(id);

            if ("LIVE".equals(session.getStatus())) {
                String token = tokenService.generateTrainerToken(id);
                Map<String, String> response = new HashMap<>();
                response.put("room", "session-" + id);
                response.put("token", token);
                return ResponseEntity.ok(response);
            }

            if (!service.canStart(session)) {
                long minutesAway = 0;
                if (session.getScheduledDate() != null && session.getScheduledTime() != null) {
                    LocalDateTime scheduledAt = LocalDateTime.of(
                        session.getScheduledDate(), session.getScheduledTime()
                    );
                    minutesAway = ChronoUnit.MINUTES.between(LocalDateTime.now(), scheduledAt);
                }
                return ResponseEntity.badRequest().body(new ErrorResponse(
                    minutesAway > 0
                        ? "Cannot start yet. Session starts in " + minutesAway + " minute(s)."
                        : "Session cannot be started in its current state."
                ));
            }

            service.startSession(id);

            String token = tokenService.generateTrainerToken(id);
            Map<String, String> response = new HashMap<>();
            response.put("room", "session-" + id);
            response.put("token", token);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to start session: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/join")
    public ResponseEntity<?> joinSession(
            @PathVariable Long id,
            Authentication auth) {
        try {
            String studentEmail = auth.getName();
            String token = tokenService.generateStudentToken(id, studentEmail);
            Map<String, String> response = new HashMap<>();
            response.put("room", "session-" + id);
            response.put("token", token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // ═══════════════════════════════════════════════════════
    // CALLS (WebSocket + LiveKit)
    // ═══════════════════════════════════════════════════════

    @PostMapping("/call/start")
    public Map<String, String> startCall(@RequestParam String trainerEmail) {
        String room = "call-" + trainerEmail + "-" + System.currentTimeMillis();
        String token = tokenService.generateCallToken("student-caller", room);

        messagingTemplate.convertAndSend("/topic/calls/" + trainerEmail, room);

        Map<String, String> res = new HashMap<>();
        res.put("room", room);
        res.put("token", token);
        return res;
    }

    @GetMapping("/call/join")
    public ResponseEntity<?> joinCall(@RequestParam String room) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            String token = tokenService.generateCallToken(email, room);

            Map<String, String> response = new HashMap<>();
            response.put("room", room);
            response.put("token", token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // ═══════════════════════════════════════════════════════
    // PARTICIPANTS
    // ═══════════════════════════════════════════════════════

    @PostMapping("/{sessionId}/participant/join")
    public ResponseEntity<?> joinSessionAsParticipant(
            @PathVariable Long sessionId,
            @RequestParam Long batchId,
            @RequestParam String trainerEmail,
            Authentication auth) {
        try {
            String studentEmail = auth.getName();
            SessionParticipant participant = participantService.joinSession(
                sessionId, batchId, studentEmail, trainerEmail
            );
            return ResponseEntity.ok(participant);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/{sessionId}/participant/leave")
    public ResponseEntity<?> leaveSessionAsParticipant(
            @PathVariable Long sessionId,
            Authentication auth) {
        try {
            String studentEmail = auth.getName();
            SessionParticipant participant = participantService.leaveSession(
                sessionId, studentEmail
            );
            return ResponseEntity.ok(participant);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{sessionId}/participants")
    public ResponseEntity<List<SessionParticipant>> getParticipants(
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(participantService.getSessionParticipants(sessionId));
    }

    @GetMapping("/{sessionId}/participant/active-count")
    public ResponseEntity<?> getActiveCount(@PathVariable Long sessionId) {
        return ResponseEntity.ok(Map.of("activeCount", participantService.getActiveCount(sessionId)));
    }

    @GetMapping("/{sessionId}/participant/has-joined")
    public ResponseEntity<?> hasJoined(
            @PathVariable Long sessionId,
            Authentication auth) {
        String studentEmail = auth.getName();
        return ResponseEntity.ok(Map.of("hasJoined", participantService.hasJoined(sessionId, studentEmail)));
    }

    // ═══════════════════════════════════════════════════════
    // PUBLIC BOOKINGS
    // ═══════════════════════════════════════════════════════

    @PostMapping("/public/bookings")
    public ResponseEntity<?> bookSession(@RequestBody PublicBookingRequest request) {
      try {
          PublicSessionBooking booking = bookingService.bookSession(
              request.getSessionId(),
              request.getFullName(),
              request.getEmail(),
              request.getPhoneNumber(),
              request.getCountry(),
              request.getGdprConsent(),
              request.getTopicsOfInterest(),
              request.getJobRole(),
              request.getHowDidYouHear(),
              request.getLearningGoal()
          );
          String joinLink = urlBuilderService.generatePublicJoinLink(booking.getUniqueAccessToken());
          return ResponseEntity.ok(new PublicBookingResponse(
              booking.getId(), booking.getSessionId(), booking.getFullName(),
              booking.getEmail(), joinLink, booking.getBookingStatus(),
              "Booking confirmed! Join link sent to your email."
          ));
      } catch (Exception e) {
          return ResponseEntity.badRequest().body(new ErrorResponse("Booking failed: " + e.getMessage()));
      }
    }

    @GetMapping("/public/bookings/verify/{token}")
    public ResponseEntity<?> verifyBooking(@PathVariable String token) {
        return bookingService.getBookingByToken(token)
            .map(booking -> {
                String joinLink = urlBuilderService.generatePublicJoinLink(booking.getUniqueAccessToken());
                return ResponseEntity.ok(new PublicBookingResponse(
                    booking.getId(), booking.getSessionId(), booking.getFullName(),
                    booking.getEmail(), joinLink, booking.getBookingStatus(),
                    "Booking verified successfully!"
                ));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/public/bookings/{id}/join")
    public ResponseEntity<?> markBookingAsJoined(@PathVariable Long id) {
        try {
            PublicSessionBooking booking = bookingService.markAsJoined(id);
            String joinLink = urlBuilderService.generatePublicJoinLink(booking.getUniqueAccessToken());
            return ResponseEntity.ok(new PublicBookingResponse(
                booking.getId(), booking.getSessionId(), booking.getFullName(),
                booking.getEmail(), joinLink, booking.getBookingStatus(), "Joined successfully!"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/public/bookings/{id}/leave")
    public ResponseEntity<?> markBookingAsLeft(@PathVariable Long id) {
        try {
            PublicSessionBooking booking = bookingService.markAsLeft(id);
            Long duration = bookingService.getBookingDuration(id);
            String joinLink = urlBuilderService.generatePublicJoinLink(booking.getUniqueAccessToken());
            return ResponseEntity.ok(new PublicBookingResponse(
                booking.getId(), booking.getSessionId(), booking.getFullName(),
                booking.getEmail(), joinLink, booking.getBookingStatus(),
                "Session ended. Duration: " + duration + " minutes"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/public/bookings/session/{sessionId}")
    public ResponseEntity<?> getSessionBookings(@PathVariable Long sessionId) {
        List<PublicSessionBooking> bookings = bookingService.getSessionBookings(sessionId);
        List<PublicBookingResponse> responses = bookings.stream()
            .map(b -> {
                String joinLink = urlBuilderService.generatePublicJoinLink(b.getUniqueAccessToken());
                return new PublicBookingResponse(
                    b.getId(), b.getSessionId(), b.getFullName(),
                    b.getEmail(), joinLink, b.getBookingStatus(), ""
                );
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/public/bookings/{id}/duration")
    public ResponseEntity<?> getBookingDuration(@PathVariable Long id) {
        try {
            Long duration = bookingService.getBookingDuration(id);
            return ResponseEntity.ok(new DurationResponse(id, duration));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/public/bookings/{id}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id) {
        try {
            PublicSessionBooking booking = bookingService.cancelBooking(id);
            String joinLink = urlBuilderService.generatePublicJoinLink(booking.getUniqueAccessToken());
            return ResponseEntity.ok(new PublicBookingResponse(
                booking.getId(), booking.getSessionId(), booking.getFullName(),
                booking.getEmail(), joinLink, booking.getBookingStatus(),
                "Booking cancelled successfully!"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/public/upcoming")
    public ResponseEntity<List<LiveSession>> getUpcomingSessions() {
        List<LiveSession> sessions = service.getUpcomingPublicSessions();
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/public/session/{id}")
    public ResponseEntity<?> getPublicSessionDetails(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.getSessionById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ═══════════════════════════════════════════════════════
    // RECORDINGS
    // ═══════════════════════════════════════════════════════

    @PostMapping("/recording/upload")
    public ResponseEntity<?> uploadRecording(
            @RequestParam("file")                                    MultipartFile file,
            @RequestParam(value = "sessionId",       required = false) Long sessionId,
            @RequestParam("batchId")                                 Long batchId,
            @RequestParam(value = "description",     required = false) String description,
            @RequestParam("title")                                   String title,
            @RequestParam(value = "batchName",       required = false) String batchName,
            @RequestParam(value = "durationMinutes", required = false) Integer durationMinutes,
            Authentication auth,
            HttpServletRequest request) { // ✅ NEW
        try {
            // ✅ NEW — reuse the same trainer-vs-batch org validation as session
            // creation, since uploading is also a write scoped to a batch.
            String token = extractRawToken(request);
            Long organizationId = accessValidator.validateAndResolveOrganizationId(token, batchId);

//            RecordingResponse response = recordingService.uploadRecording(
//                file, sessionId, batchId, auth.getName(),
//                title, description, batchName, durationMinutes,
//                organizationId // ✅ NEW
//            );
//            return ResponseEntity.ok(response);
//        } catch (LiveSessionAccessDeniedException e) { // ✅ NEW
//            return ResponseEntity.badRequest().body(new ErrorResponse("Access denied: " + e.getMessage()));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(new ErrorResponse("Upload failed: " + e.getMessage()));
//        }
//    }
            RecordingResponse response = recordingService.uploadRecording(
                    file, sessionId, batchId, auth.getName(),
                    title, description, batchName, durationMinutes,
                    organizationId // ✅ NEW
                );
                return ResponseEntity.ok(response);
            } catch (LiveSessionAccessDeniedException e) { // ✅ NEW
                return ResponseEntity.badRequest().body(new ErrorResponse("Access denied: " + e.getMessage()));
            } catch (com.lms.live_session.exception.RecordingStorageLimitExceededException
                    | com.lms.live_session.exception.RecordingDurationLimitExceededException e) {
                throw e; // let GlobalExceptionHandler produce the documented 403 + error code
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Upload failed: " + e.getMessage()));
            }
        }

    @GetMapping("/recording/all")
    public ResponseEntity<List<RecordingResponse>> getAllRecordings(HttpServletRequest request) { // ✅ NEW param
        return ResponseEntity.ok(recordingService.getAllRecordings(resolveCallerOrgId(request))); // ✅ CHANGED
    }

    @GetMapping("/recording/batch/{batchId}")
    public ResponseEntity<List<RecordingResponse>> getRecordingsByBatch(
            @PathVariable Long batchId, HttpServletRequest request) { // ✅ NEW param
        return ResponseEntity.ok(recordingService.getByBatch(batchId, resolveCallerOrgId(request))); // ✅ CHANGED
    }

    @GetMapping("/recording/session/{sessionId}")
    public ResponseEntity<List<RecordingResponse>> getRecordingsBySession(
            @PathVariable Long sessionId, HttpServletRequest request) { // ✅ NEW param
        return ResponseEntity.ok(recordingService.getBySession(sessionId, resolveCallerOrgId(request))); // ✅ CHANGED
    }

    @GetMapping("/recording/trainer/my")
    public ResponseEntity<List<RecordingResponse>> getMyRecordings(Authentication auth) {
        return ResponseEntity.ok(recordingService.getByTrainerEmail(auth.getName()));
    }

    @GetMapping("/recording/{id}")
    public ResponseEntity<?> getRecordingById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(recordingService.getById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/recording/{id}/view")
    public ResponseEntity<?> incrementView(@PathVariable Long id) {
        try {
            recordingService.incrementViews(id);
            return ResponseEntity.ok(Map.of("message", "View counted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/recording/{id}")
    public ResponseEntity<?> updateRecording(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(recordingService.updateRecording(
                id, body.get("title"), body.get("description")
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/recording/{id}")
    public ResponseEntity<?> deleteRecording(@PathVariable Long id) {
        try {
            recordingService.deleteRecording(id);
            return ResponseEntity.ok(Map.of("message", "Recording deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/recording/{id}/mark-ready")
    public ResponseEntity<?> markRecordingReady(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(recordingService.markAsReady(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/recording/{id}/mark-failed")
    public ResponseEntity<?> markRecordingFailed(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(recordingService.markAsFailed(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // ═══════════════════════════════════════════════════════
    // HELPER RESPONSE CLASSES
    // ═══════════════════════════════════════════════════════

    static class ErrorResponse {
        public String error;
        public ErrorResponse(String error) { this.error = error; }
    }

    static class DurationResponse {
        public Long bookingId;
        public Long durationMinutes;
        public DurationResponse(Long bookingId, Long durationMinutes) {
            this.bookingId       = bookingId;
            this.durationMinutes = durationMinutes;
        }
    }

    @PostMapping("/{id}/recording/start")
    public ResponseEntity<?> startRecordingEndpoint(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.enableRecording(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/{id}/recording/stop")
    public ResponseEntity<?> stopRecordingEndpoint(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.disableRecording(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    
}