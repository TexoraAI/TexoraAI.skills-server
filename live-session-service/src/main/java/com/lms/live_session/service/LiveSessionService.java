package com.lms.live_session.service;

import com.lms.live_session.entity.LiveSession;
import com.lms.live_session.event.LiveSessionEvent;
import com.lms.live_session.event.SessionNotificationEvent;
import com.lms.live_session.kafka.LiveSessionProducer;
import com.lms.live_session.kafka.NotificationProducer;
import com.lms.live_session.repository.LiveSessionRepository;
import com.lms.live_session.repository.StudentBatchMapRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class LiveSessionService {

    private final LiveSessionRepository repository;
    private final LiveSessionProducer producer;
    private final NotificationProducer notificationProducer;
    private final StudentBatchMapRepository studentBatchMapRepository;
    private final UrlBuilderService urlBuilderService;
    private final EgressService egressService;
    private final RecordingService recordingService;

    public LiveSessionService(
            LiveSessionRepository repository,
            LiveSessionProducer producer,
            NotificationProducer notificationProducer,
            StudentBatchMapRepository studentBatchMapRepository,
            UrlBuilderService urlBuilderService,
            EgressService egressService,
            RecordingService recordingService) {
        this.repository               = repository;
        this.producer                 = producer;
        this.notificationProducer     = notificationProducer;
        this.studentBatchMapRepository = studentBatchMapRepository;
        this.urlBuilderService        = urlBuilderService;
        this.egressService = egressService;
        this.recordingService = recordingService;
    }

    // ─────────────────────────────────────────────────────────────────
    // CREATE SESSION
    // ─────────────────────────────────────────────────────────────────

    public LiveSession createSession(LiveSession session) {
        session.setStatus("SCHEDULED");
        if (session.getMeetingType() == null) {
            session.setMeetingType("CUSTOM");
        }
        // createdAt is set by @PrePersist
        LiveSession saved = repository.save(session);

        // Kafka: CREATED event
        producer.publishLiveStarted(new LiveSessionEvent(
            saved.getId(), saved.getBatchId(), saved.getTrainerEmail(), "CREATED"
        ));

        // ✅ BUG 3 FIX:
        // If session is scheduled LESS than 30 minutes from now,
        // the 15-min scheduler will never fire (it checks 14-15 min window).
        // So send an IMMEDIATE notification to students at creation time.
        sendImmediateNotificationIfNeeded(saved);

        return saved;
    }

    // ─────────────────────────────────────────────────────────────────
    // START SESSION → records actual start time (BUG 2 FIX)
    // ─────────────────────────────────────────────────────────────────

    public LiveSession startSession(Long id) {
        LiveSession session = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Session not found: " + id));

        session.setStatus("LIVE");
        // ✅ BUG 2 FIX: record the real start time so auto-end uses THIS not scheduledTime
        session.setActualStartTime(LocalDateTime.now());
        
        // ✅ Start recording
        String egressId = egressService.startRecording(id);
        if (egressId != null) {
            session.setEgressId(egressId);
        }


        LiveSession saved = repository.save(session);

        producer.publishLiveStarted(new LiveSessionEvent(
            saved.getId(), saved.getBatchId(), saved.getTrainerEmail(), "STARTED"
        ));

        return saved;
    }

    // ─────────────────────────────────────────────────────────────────
    // END SESSION → records actual end time
    // ─────────────────────────────────────────────────────────────────
    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.region}")
    private String awsRegion;
    public LiveSession endSession(Long id) {
        LiveSession session = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Session not found: " + id));

        session.setStatus("ENDED");
        session.setActualEndTime(LocalDateTime.now());
        // ✅ Stop recording and save S3 URL
        if (session.getEgressId() != null) {
            egressService.stopRecording(session.getEgressId());

            String s3Url = "https://" + bucket + ".s3." + awsRegion
                + ".amazonaws.com/recordings/session-" + id + ".mp4";
            session.setRecordingS3Url(s3Url);

            // ✅ Save to recordings table so it shows in Recorded Classes List
            recordingService.createAutoRecordPlaceholder(
                id,
                session.getBatchId(),
                session.getTrainerEmail(),
                session.getTitle(),
                s3Url
            );
        }

        LiveSession saved = repository.save(session);

        producer.publishLiveStarted(new LiveSessionEvent(
            saved.getId(), saved.getBatchId(), saved.getTrainerEmail(), "ENDED"
        ));

        return saved;
    }

    // ─────────────────────────────────────────────────────────────────
    // CAN START CHECK (used by /can-start endpoint)
    // ─────────────────────────────────────────────────────────────────

    /**
     * Rules:
     *  1. Status must be SCHEDULED
     *  2. If no schedule set → allow (safe fallback)
     *  3. If session was created with < 15 min gap → allow only AT or AFTER scheduled time
     *  4. Normal case → allow within 15 min before scheduled time
     */
    public boolean canStart(LiveSession session) {
        if (!"SCHEDULED".equals(session.getStatus())) return false;
        if (session.getScheduledDate() == null || session.getScheduledTime() == null) return true;

        LocalDateTime scheduledAt = LocalDateTime.of(
            session.getScheduledDate(), session.getScheduledTime()
        );
        LocalDateTime now = LocalDateTime.now();
        long diffMinutes = ChronoUnit.MINUTES.between(now, scheduledAt);

        // ✅ BUG 1 FIX: If session was scheduled < 15 min from creation,
        // only unlock at the actual scheduled time
        if (session.getCreatedAt() != null) {
            long gapFromCreation = ChronoUnit.MINUTES.between(
                session.getCreatedAt(), scheduledAt
            );
            if (gapFromCreation < 15) {
                // Only allow at/after scheduled time (diff <= 0)
                return diffMinutes <= 0;
            }
        }

        // Normal case: allow 15 min before scheduled time
        return diffMinutes <= 15;
    }

    // ─────────────────────────────────────────────────────────────────
    // IMMEDIATE NOTIFICATION FOR SHORT-SCHEDULED SESSIONS (BUG 3 FIX)
    // ─────────────────────────────────────────────────────────────────

    private void sendImmediateNotificationIfNeeded(LiveSession session) {
        if (session.getScheduledDate() == null || session.getScheduledTime() == null) return;
        if (Boolean.FALSE.equals(session.getNotifyStudents())) return;

        LocalDateTime scheduledAt = LocalDateTime.of(
            session.getScheduledDate(), session.getScheduledTime()
        );
        long minutesAway = ChronoUnit.MINUTES.between(LocalDateTime.now(), scheduledAt);

        // Only send immediate alert if session starts in < 30 min
        // (the 15-min scheduler won't reach it in time)
        if (minutesAway <= 0 || minutesAway >= 30) return;

        List<com.lms.live_session.entity.StudentBatchMap> students =
            studentBatchMapRepository.findByBatchId(session.getBatchId());

        for (com.lms.live_session.entity.StudentBatchMap student : students) {
            try {
                String studentLink = urlBuilderService.generateStudentLiveLink(session.getId());

                SessionNotificationEvent event = new SessionNotificationEvent(
                    session.getId(),
                    session.getTrainerEmail(),
                    session.getBatchId(),
                    session.getTitle(),
                    session.getScheduledDate().toString(),
                    session.getScheduledTime().toString(),
                    session.getDuration(),
                    "STUDENT_IMMEDIATE_ALERT",   // ✅ new event type handled in notification-service
                    student.getStudentEmail(),
                    student.getStudentEmail(),
                    "STUDENT",
                    studentLink
                );
                notificationProducer.sendStudentReminder(event);

                System.out.println("⚡ Immediate alert sent to: " + student.getStudentEmail()
                    + " — session in " + minutesAway + " min");

            } catch (Exception e) {
                System.err.println("❌ Immediate notification failed: " + e.getMessage());
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // QUERIES
    // ─────────────────────────────────────────────────────────────────

    public List<LiveSession> getBatchSessions(Long batchId) {
        return repository.findByBatchId(batchId);
    }

    public List<LiveSession> getLiveSessions(Long batchId) {
        return repository.findByBatchIdAndStatus(batchId, "LIVE");
    }

    public List<LiveSession> getMySessionsAsTrainer(String trainerEmail) {
        return repository.findByTrainerEmailOrderByScheduledDateDesc(trainerEmail);
    }

    public List<LiveSession> getMyLiveSessionsAsTrainer(String trainerEmail) {
        return repository.findByTrainerEmailAndStatus(trainerEmail, "LIVE");
    }

    public List<LiveSession> getEndedSessions() {
        return repository.findByStatus("ENDED");
    }

    public LiveSession getSessionById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Session not found: " + id));
    }

    public void deleteSession(Long id) {
        LiveSession session = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Session not found: " + id));
        repository.delete(session);
    }
    public List<LiveSession> getUpcomingPublicSessions() {
        return repository.findByStatusIn(List.of("SCHEDULED", "LIVE"));
    }
    
 // ADD these 3 methods to LiveSessionService — don't touch existing ones

 // ── Resolve meeting URL for a session ────────────────────────
 // Returns external URL if EXTERNAL type, else generates LiveKit custom URL
 public Map<String, String> resolveMeetingLink(Long id) {
     LiveSession session = getSessionById(id);
     Map<String, String> result = new java.util.HashMap<>();

     if ("EXTERNAL".equals(session.getMeetingType())
             && session.getExternalMeetingUrl() != null
             && !session.getExternalMeetingUrl().isBlank()) {
         result.put("type", "EXTERNAL");
         result.put("url", session.getExternalMeetingUrl());
     } else {
         // CUSTOM — caller will request LiveKit token separately
         result.put("type", "CUSTOM");
         result.put("url", urlBuilderService.generateTrainerLiveLink(session.getId()));
     }
     return result;
 }

 // ── Calendar: trainer's sessions between two dates ───────────
 public List<LiveSession> getTrainerCalendar(
         String trainerEmail, LocalDate from, LocalDate to) {
     return repository.findByTrainerEmailAndScheduledDateBetween(
         trainerEmail, from, to);
 }

 // ── Published sessions (global, no batchId needed) ───────────
 public List<LiveSession> getPublishedSessions() {
     return repository.findByIsPublishedTrueAndStatusIn(
         List.of("SCHEDULED", "LIVE"));
 }
}