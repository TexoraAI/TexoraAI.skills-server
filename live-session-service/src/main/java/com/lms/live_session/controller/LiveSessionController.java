package com.lms.live_session.controller;

import com.lms.live_session.dto.PublicBookingRequest;
import com.lms.live_session.dto.PublicBookingResponse;
import com.lms.live_session.dto.RecordingResponse;
import com.lms.live_session.entity.LiveSession;
import com.lms.live_session.entity.PublicSessionBooking;
import com.lms.live_session.entity.SessionParticipant;
import com.lms.live_session.service.*;
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

    public LiveSessionController(
            LiveSessionService service,
            LiveKitTokenService tokenService,
            SimpMessagingTemplate messagingTemplate,
            ParticipantService participantService,
            PublicBookingService bookingService,
            UrlBuilderService urlBuilderService,
            RecordingService recordingService) {
        this.service           = service;
        this.tokenService      = tokenService;
        this.messagingTemplate = messagingTemplate;
        this.participantService = participantService;
        this.bookingService    = bookingService;
        this.urlBuilderService = urlBuilderService;
        this.recordingService  = recordingService;
    }

    // ═══════════════════════════════════════════════════════
    // LIVE SESSION CRUD
    // ═══════════════════════════════════════════════════════

    @PostMapping
    public ResponseEntity<?> createSession(@RequestBody LiveSession session, Authentication auth) {
        try {
            session.setTrainerEmail(auth.getName());
            LiveSession created = service.createSession(session);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to create session: " + e.getMessage()));
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
    public List<LiveSession> getBatchSessions(@PathVariable Long batchId) {
        return service.getBatchSessions(batchId);
    }

    @GetMapping("/batch/{batchId}/live")
    public List<LiveSession> getLiveSessions(@PathVariable Long batchId) {
        return service.getLiveSessions(batchId);
    }

 // ADD these 3 endpoints to LiveSessionController — don't touch existing ones

 // ── Resolve meeting link (external vs custom) ─────────────────
 // Frontend calls this when trainer clicks "Go Live" or student joins
 // If EXTERNAL → redirect to Zoom/Meet URL
 // If CUSTOM   → proceed to get LiveKit token
 @GetMapping("/{id}/meeting-link")
 public ResponseEntity<?> getMeetingLink(@PathVariable Long id) {
     try {
         return ResponseEntity.ok(service.resolveMeetingLink(id));
     } catch (Exception e) {
         return ResponseEntity.badRequest()
             .body(new ErrorResponse(e.getMessage()));
     }
 }

 // ── Trainer calendar view ─────────────────────────────────────
 // GET /api/live-sessions/calendar?from=2025-05-01&to=2025-05-31
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

 // ── Published/global sessions (no batchId, anyone can view) ──
 // GET /api/live-sessions/published
 @GetMapping("/published")
 public ResponseEntity<List<LiveSession>> getPublishedSessions() {
     return ResponseEntity.ok(service.getPublishedSessions());
 }
    
    // ═══════════════════════════════════════════════════════
    // CAN-START CHECK
    // ✅ BUG 1 FIX: Uses service.canStart() which considers createdAt gap
    // Frontend calls this to decide whether to show "Go Live" button
    // GET /api/live-sessions/{id}/can-start
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

    /**
     * POST /api/live-sessions/{id}/start-live
     * Checks canStart → sets status LIVE → records actualStartTime → returns LiveKit token
     */
//    @PostMapping("/{id}/start-live")
//    public ResponseEntity<?> startLiveSession(@PathVariable Long id, Authentication auth) {
//        try {
//            LiveSession session = service.getSessionById(id);
//
//            // ✅ BUG 1 FIX: enforce canStart check server-side too
//            if (!service.canStart(session)) {
//                long minutesAway = 0;
//                if (session.getScheduledDate() != null && session.getScheduledTime() != null) {
//                    LocalDateTime scheduledAt = LocalDateTime.of(
//                        session.getScheduledDate(), session.getScheduledTime()
//                    );
//                    minutesAway = ChronoUnit.MINUTES.between(LocalDateTime.now(), scheduledAt);
//                }
//                return ResponseEntity.badRequest().body(new ErrorResponse(
//                    minutesAway > 0
//                        ? "Cannot start yet. Session starts in " + minutesAway + " minute(s)."
//                        : "Session cannot be started in its current state."
//                ));
//            }
//
//            // ✅ BUG 2 FIX: actualStartTime is set inside service.startSession()
//            service.startSession(id);
//
//            String token = tokenService.generateTrainerToken(id);
//            Map<String, String> response = new HashMap<>();
//            response.put("room", "session-" + id);
//            response.put("token", token);
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to start session: " + e.getMessage()));
//        }
//    }
    @PostMapping("/{id}/start-live")
    public ResponseEntity<?> startLiveSession(@PathVariable Long id, Authentication auth) {
        try {
            LiveSession session = service.getSessionById(id);

            // ✅ FIX: If already LIVE, just return a new token (trainer re-joining)
            if ("LIVE".equals(session.getStatus())) {
                String token = tokenService.generateTrainerToken(id);
                Map<String, String> response = new HashMap<>();
                response.put("room", "session-" + id);
                response.put("token", token);
                return ResponseEntity.ok(response);
            }

            // Only enforce canStart for SCHEDULED → LIVE transition
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

            service.startSession(id);  // sets LIVE + actualStartTime

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
            @RequestParam Long studentId) {
        try {
            String token = tokenService.generateStudentToken(id, studentId);
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
//
//    @PostMapping("/{sessionId}/participant/join")
//    public ResponseEntity<?> joinSessionAsParticipant(
//            @PathVariable Long sessionId,
//            @RequestParam Long batchId,
//            @RequestParam String studentEmail,
//            @RequestParam String trainerEmail) {
//        try {
//            SessionParticipant participant = participantService.joinSession(
//                sessionId, batchId, studentEmail, trainerEmail
//            );
//            return ResponseEntity.ok(participant);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
//        }
//    }
//
//    @PostMapping("/{sessionId}/participant/leave")
//    public ResponseEntity<?> leaveSessionAsParticipant(
//            @PathVariable Long sessionId,
//            @RequestParam String studentEmail) {
//        try {
//            SessionParticipant participant = participantService.leaveSession(
//                sessionId, studentEmail
//            );
//            return ResponseEntity.ok(participant);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
//        }
//    }
//
//    @GetMapping("/{sessionId}/participants")
//    public ResponseEntity<List<SessionParticipant>> getParticipants(@PathVariable Long sessionId) {
//        return ResponseEntity.ok(participantService.getSessionParticipants(sessionId));
//    }
 // ═══════════════════════════════════════════════════════
 // PARTICIPANTS — email extracted from JWT, not from params
 // ═══════════════════════════════════════════════════════

 @PostMapping("/{sessionId}/participant/join")
 public ResponseEntity<?> joinSessionAsParticipant(
         @PathVariable Long sessionId,
         @RequestParam Long batchId,
         @RequestParam String trainerEmail,
         Authentication auth) {          // ✅ studentEmail comes from JWT
     try {
         String studentEmail = auth.getName();  // extracted from JWT
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
         Authentication auth) {          // ✅ studentEmail comes from JWT
     try {
         String studentEmail = auth.getName();  // extracted from JWT
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
         Authentication auth) {          // ✅ from JWT
     String studentEmail = auth.getName();
     return ResponseEntity.ok(Map.of("hasJoined", participantService.hasJoined(sessionId, studentEmail)));
 }
    // ═══════════════════════════════════════════════════════
    // PUBLIC BOOKINGS
    // ═══════════════════════════════════════════════════════

//    @PostMapping("/public/bookings")
//    public ResponseEntity<?> bookSession(@RequestBody PublicBookingRequest request) {
//        try {
//            PublicSessionBooking booking = bookingService.bookSession(
//                request.getSessionId(),
//                request.getFullName(),
//                request.getEmail(),
//                request.getPhoneNumber(),
//                request.getCountry(),
//                request.getGdprConsent()
//            );
//            String joinLink = urlBuilderService.generatePublicJoinLink(booking.getUniqueAccessToken());
//            return ResponseEntity.ok(new PublicBookingResponse(
//                booking.getId(), booking.getSessionId(), booking.getFullName(),
//                booking.getEmail(), joinLink, booking.getBookingStatus(),
//                "Booking confirmed! Join link: " + joinLink
//            ));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(new ErrorResponse("Booking failed: " + e.getMessage()));
//        }
//    }
//── REPLACE ONLY THIS METHOD inside LiveSessionController ──────────
//Everything else in LiveSessionController stays exactly the same.

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
          // ✅ 4 new fields
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
            Authentication auth) {
        try {
            RecordingResponse response = recordingService.uploadRecording(
                file, sessionId, batchId, auth.getName(),
                title, description, batchName, durationMinutes
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Upload failed: " + e.getMessage()));
        }
    }

    @GetMapping("/recording/all")
    public ResponseEntity<List<RecordingResponse>> getAllRecordings() {
        return ResponseEntity.ok(recordingService.getAllRecordings());
    }

    @GetMapping("/recording/batch/{batchId}")
    public ResponseEntity<List<RecordingResponse>> getRecordingsByBatch(@PathVariable Long batchId) {
        return ResponseEntity.ok(recordingService.getByBatch(batchId));
    }

    @GetMapping("/recording/session/{sessionId}")
    public ResponseEntity<List<RecordingResponse>> getRecordingsBySession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(recordingService.getBySession(sessionId));
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
}