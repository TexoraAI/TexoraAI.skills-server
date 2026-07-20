package com.lms.live_session.service;

import com.lms.live_session.entity.LiveSession;
import java.time.ZoneId;
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



 // START SESSION → atomic guard on SCHEDULED -> LIVE, atomic egress claim
 // ─────────────────────────────────────────────────────────────────

 public LiveSession startSession(Long id) {
     long ts = System.currentTimeMillis();
     LiveSession snapshot = repository.findById(id)
         .orElseThrow(() -> new RuntimeException("Session not found: " + id));

     System.out.println("[startSession] ENTRY sessionId=" + id
         + " dbStatus=" + snapshot.getStatus()
         + " dbEgressId=" + snapshot.getEgressId()
         + " thread=" + Thread.currentThread().getName()
         + " ts=" + ts);

     int rows = repository.atomicMarkLive(id, LocalDateTime.now());

     if (rows == 0) {
         // Another concurrent call already flipped this session to LIVE.
         // It — and only it — is responsible for egress/notifications.
         System.out.println("[startSession] BLOCKED duplicate start attempt for session " + id
             + " thread=" + Thread.currentThread().getName());
         return repository.findById(id)
             .orElseThrow(() -> new RuntimeException("Session not found: " + id));
     }

     // We won the race for this session's SCHEDULED -> LIVE transition.
     LiveSession session = repository.findById(id)
         .orElseThrow(() -> new RuntimeException("Session not found: " + id));

     if (Boolean.TRUE.equals(session.getAutoRecord())) {
         session = claimAndStartEgress(session, "startSession");
     }

     producer.publishLiveStarted(new LiveSessionEvent(
         session.getId(), session.getBatchId(), session.getTrainerEmail(), "STARTED"
     ));

     sendStudentLiveNowNotification(session);

     return session;
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

        if (session.getEgressId() != null) {
            String egressIdToStop = session.getEgressId();

            // ✅ CHANGED — same pattern as disableRecording: read the real
            // filename from LiveKit instead of guessing it from egressId.
            livekit.LivekitEgress.EgressInfo info = egressService.stopRecordingAndGetInfo(egressIdToStop);

            if (info != null && info.getFileResultsCount() > 0) {
                String realFilename = info.getFileResults(0).getFilename();
                String s3Url = "https://" + bucket + ".s3." + awsRegion + ".amazonaws.com/" + realFilename;
                session.setRecordingS3Url(s3Url);

                int partNumber = recordingService.getBySession(id).size() + 1;
                recordingService.createAutoRecordPlaceholder(
                    id,
                    session.getBatchId(),
                    session.getTrainerEmail(),
                    session.getTitle() + " — Part " + partNumber,
                    s3Url
                );
            } else {
                System.err.println("[endSession] No usable EgressInfo/file results for " + egressIdToStop
                    + " — NOT creating a recordings row (avoiding a broken/guessed URL).");
            }

            session.setEgressId(null);
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

   
    public boolean canStart(LiveSession session) {
        if (!"SCHEDULED".equals(session.getStatus())) return false;
        if (session.getScheduledDate() == null || session.getScheduledTime() == null) return true;

        LocalDateTime scheduledAt = LocalDateTime.of(
            session.getScheduledDate(), session.getScheduledTime()
        );
//        LocalDateTime now = LocalDateTime.now();
        ZoneId zone = ZoneId.of(
        	    session.getTimezone() != null ? session.getTimezone() : "UTC"
        	);
        	LocalDateTime now = LocalDateTime.now(zone);
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

        // ✅ TIMEZONE FIX: same zone-aware pattern as canStart()
        ZoneId zone = ZoneId.of(
            session.getTimezone() != null ? session.getTimezone() : "UTC"
        );
        LocalDateTime scheduledAt = LocalDateTime.of(
            session.getScheduledDate(), session.getScheduledTime()
        );
        long minutesAway = ChronoUnit.MINUTES.between(LocalDateTime.now(zone), scheduledAt);

        // Only send this notification if session starts in < 30 min
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
                    "STUDENT_SESSION_CREATED",   // ✅ CHANGED — calm confirmation, not an urgent alert
                    student.getStudentEmail(),
                    student.getStudentEmail(),
                    "STUDENT",
                    studentLink
                );
                notificationProducer.sendStudentReminder(event);

                System.out.println("📩 Session-created notice sent to: " + student.getStudentEmail()
                    + " — session in " + minutesAway + " min");

            } catch (Exception e) {
                System.err.println("❌ Session-created notification failed: " + e.getMessage());
            }
        }
    }
    
    
 // ─────────────────────────────────────────────────────────────────
    // NOTIFY STUDENTS WHEN SESSION GOES LIVE (NEW)
    // ─────────────────────────────────────────────────────────────────

    private void sendStudentLiveNowNotification(LiveSession session) {
        if (Boolean.FALSE.equals(session.getNotifyStudents())) return;
        if (session.getBatchId() == null) return; // no batch roster to notify

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
                    session.getScheduledDate() != null ? session.getScheduledDate().toString() : null,
                    session.getScheduledTime() != null ? session.getScheduledTime().toString() : null,
                    session.getDuration(),
                    "STUDENT_SESSION_LIVE_NOW",   // ✅ new event type — handled in notification-service
                    student.getStudentEmail(),
                    student.getStudentEmail(),
                    "STUDENT",
                    studentLink
                );
                notificationProducer.sendStudentReminder(event);

                System.out.println("🔴 Live-now notice sent to: " + student.getStudentEmail());

            } catch (Exception e) {
                System.err.println("❌ Live-now notification failed: " + e.getMessage());
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


 // ✅ MID-SESSION RECORDING TOGGLE (atomic-claim protected)
//─────────────────────────────────────────────────────────────────

public LiveSession enableRecording(Long id) {
  System.out.println("[enableRecording] ENTRY sessionId=" + id
      + " thread=" + Thread.currentThread().getName()
      + " ts=" + System.currentTimeMillis());

  LiveSession session = repository.findById(id)
      .orElseThrow(() -> new RuntimeException("Session not found: " + id));

  System.out.println("[enableRecording] sessionId=" + id
      + " dbStatus=" + session.getStatus()
      + " dbEgressId=" + session.getEgressId());

  if (!"LIVE".equals(session.getStatus())) {
      throw new RuntimeException("Cannot start recording — session is not LIVE.");
  }

  if (session.getEgressId() != null) {
      return session; // already recording (or a concurrent start is in flight) — no-op
  }

  LiveSession updated = claimAndStartEgress(session, "enableRecording");

  if (updated.getEgressId() == null) {
      throw new RuntimeException("Failed to start recording. Check LiveKit/egress worker logs.");
  }

  if (!Boolean.TRUE.equals(updated.getAutoRecord())) {
      updated.setAutoRecord(true);
      updated = repository.save(updated);
  }

  return updated;
}

public LiveSession disableRecording(Long id) {
    System.out.println("[disableRecording] ENTRY sessionId=" + id
        + " thread=" + Thread.currentThread().getName()
        + " ts=" + System.currentTimeMillis());

    LiveSession session = repository.findById(id)
        .orElseThrow(() -> new RuntimeException("Session not found: " + id));

    System.out.println("[disableRecording] sessionId=" + id
        + " dbStatus=" + session.getStatus()
        + " dbEgressId=" + session.getEgressId());

    if (session.getEgressId() == null) {
        session.setAutoRecord(false);
        return repository.save(session); // nothing recording — no-op
    }

    String egressIdToStop = session.getEgressId();

    // ✅ CHANGED — stop and get LiveKit's own EgressInfo back, which
    // contains the REAL filename(s) that were actually uploaded to S3.
    livekit.LivekitEgress.EgressInfo info = egressService.stopRecordingAndGetInfo(egressIdToStop);

    if (info == null) {
        // Genuine failure OR "already gone" with unknown result — do NOT
        // create a recordings row with a guessed URL. Clear the egress
        // slot so the session isn't stuck, but don't pretend we have a file.
        System.err.println("[disableRecording] stop returned no EgressInfo for " + egressIdToStop
            + " — NOT creating a recordings row (avoiding a broken/guessed URL).");
        repository.atomicClearEgressId(id, egressIdToStop);
        LiveSession fallback = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Session not found: " + id));
        fallback.setAutoRecord(false);
        return repository.save(fallback);
    }

    if (info.getFileResultsCount() == 0) {
        System.err.println("[disableRecording] EgressInfo for " + egressIdToStop
            + " has NO file results — recording likely produced no output. Skipping recordings row.");
        repository.atomicClearEgressId(id, egressIdToStop);
        LiveSession fallback = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Session not found: " + id));
        fallback.setAutoRecord(false);
        return repository.save(fallback);
    }

    // ✅ Real filename straight from LiveKit — this is guaranteed to match
    // what's actually in S3.
    String realFilename = info.getFileResults(0).getFilename();
    String s3Url = "https://" + bucket + ".s3." + awsRegion + ".amazonaws.com/" + realFilename;

    int partNumber = recordingService.getBySession(id).size() + 1;

    recordingService.createAutoRecordPlaceholder(
        id,
        session.getBatchId(),
        session.getTrainerEmail(),
        session.getTitle() + " — Part " + partNumber,
        s3Url
    );

    repository.atomicClearEgressId(id, egressIdToStop);
    LiveSession freshSession = repository.findById(id)
        .orElseThrow(() -> new RuntimeException("Session not found: " + id));
    freshSession.setAutoRecord(false);
    return repository.save(freshSession);
}
//─────────────────────────────────────────────────────────────────
//✅ NEW — shared atomic egress-claim helper.
//Used by BOTH startSession() (auto-record) and enableRecording()
//(manual button) so the two paths can never both succeed for the
//same session — whichever calls atomicClaimEgressSlot first wins.
//─────────────────────────────────────────────────────────────────
private LiveSession claimAndStartEgress(LiveSession session, String caller) {

    Long id = session.getId();

    String claimToken = "PENDING:" + java.util.UUID.randomUUID();



    int claimed = repository.atomicClaimEgressSlot(id, claimToken);

    if (claimed == 0) {

        System.out.println("[" + caller + "] BLOCKED duplicate start attempt for session " + id

            + " (egress slot already held) thread=" + Thread.currentThread().getName());

        return repository.findById(id)

            .orElseThrow(() -> new RuntimeException("Session not found: " + id));

    }



    System.out.println("[" + caller + "] ABOUT TO START EGRESS for session " + id

        + ", current egressId=" + claimToken

        + " thread=" + Thread.currentThread().getName()

        + " ts=" + System.currentTimeMillis());



    // ✅ CHANGED — startRecording no longer takes fileSuffix; it generates

    // one internally and hands it back to us via EgressStartResult.

    EgressService.EgressStartResult result = egressService.startRecording(id);



    if (result == null) {

        repository.atomicClearEgressId(id, claimToken);

        System.err.println("[" + caller + "] Egress failed to start for session " + id

            + " — released claim slot, thread=" + Thread.currentThread().getName());

        return repository.findById(id)

            .orElseThrow(() -> new RuntimeException("Session not found: " + id));

    }



    int finalized = repository.atomicFinalizeEgressId(id, claimToken, result.egressId);

    if (finalized == 0) {

        System.err.println("[" + caller + "] Could not finalize egressId for session " + id

            + " — stopping orphaned egress " + result.egressId);

        egressService.stopRecording(result.egressId);

        return repository.findById(id)

            .orElseThrow(() -> new RuntimeException("Session not found: " + id));

    }



    // ✅ NEW — persist the fileSuffix too, as a fallback in case we ever

    // need to reconstruct the URL manually (we shouldn't need to, since

    // stop now reads the real filename from LiveKit's EgressInfo response).

    LiveSession fresh = repository.findById(id)

        .orElseThrow(() -> new RuntimeException("Session not found: " + id));

    fresh.setCurrentEgressFileSuffix(result.fileSuffix);

    return repository.save(fresh);

}


}