
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
    private final AiWorkflowExecutionService aiWorkflowExecutionService;
    private final LiveSessionUsageService usageService;

    public LiveSessionService(
            LiveSessionRepository repository,
            LiveSessionProducer producer,
            NotificationProducer notificationProducer,
            StudentBatchMapRepository studentBatchMapRepository,
            UrlBuilderService urlBuilderService,
            EgressService egressService,
            RecordingService recordingService,
            AiWorkflowExecutionService aiWorkflowExecutionService,
            LiveSessionUsageService usageService) {
        this.repository               = repository;
        this.producer                 = producer;
        this.notificationProducer     = notificationProducer;
        this.studentBatchMapRepository = studentBatchMapRepository;
        this.urlBuilderService        = urlBuilderService;
        this.egressService = egressService;
        this.recordingService = recordingService;
        this.aiWorkflowExecutionService = aiWorkflowExecutionService;
        this.usageService = usageService;
    }

    // ─────────────────────────────────────────────────────────────────
    // CREATE SESSION
    // ─────────────────────────────────────────────────────────────────

    public LiveSession createSession(LiveSession session) {
        usageService.checkAndIncrementClassCreation(session.getOrganizationId(), session.getTrainerEmail());
        session.setStatus("SCHEDULED");
        if (session.getMeetingType() == null) {
            session.setMeetingType("CUSTOM");
        }
        LiveSession saved = repository.save(session);

        producer.publishLiveStarted(new LiveSessionEvent(
            saved.getId(), saved.getBatchId(), saved.getTrainerEmail(), "CREATED"
        ));

        sendImmediateNotificationIfNeeded(saved);

        try {
            aiWorkflowExecutionService.fireTrigger(
                AiWorkflowExecutionService.TRIGGER_SESSION_SCHEDULED,
                saved.getId(),
                saved.getTrainerEmail()
            );
        } catch (Exception e) {
            System.err.println("[LiveSessionService] Failed to fire session-scheduled trigger: " + e.getMessage());
        }

        return saved;
    }

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
            System.out.println("[startSession] BLOCKED duplicate start attempt for session " + id
                + " thread=" + Thread.currentThread().getName());
            return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found: " + id));
        }

        LiveSession session = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Session not found: " + id));

        if (Boolean.TRUE.equals(session.getAutoRecord())) {
            session = claimAndStartEgress(session, "startSession");
        }

        producer.publishLiveStarted(new LiveSessionEvent(
            session.getId(), session.getBatchId(), session.getTrainerEmail(), "STARTED"
        ));

        sendStudentLiveNowNotification(session);

        try {
            aiWorkflowExecutionService.fireTrigger(
                AiWorkflowExecutionService.TRIGGER_SESSION_STARTED,
                session.getId(),
                session.getTrainerEmail()
            );
        } catch (Exception e) {
            System.err.println("[LiveSessionService] Failed to fire session-started trigger: " + e.getMessage());
        }

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

            livekit.LivekitEgress.EgressInfo info = egressService.stopRecordingAndGetInfo(egressIdToStop);

            if (info != null && info.getFileResultsCount() > 0) {
                String realFilename = info.getFileResults(0).getFilename();
                String s3Url = "https://" + bucket + ".s3." + awsRegion + ".amazonaws.com/" + realFilename;
                session.setRecordingS3Url(s3Url);

                int partNumber = recordingService.getBySession(id, null).size() + 1; // ✅ CHANGED — internal call, org filter not applicable here (see note below)
                recordingService.createAutoRecordPlaceholder(
                    id,
                    session.getBatchId(),
                    session.getTrainerEmail(),
                    session.getTitle() + " — Part " + partNumber,
                    s3Url,
                    session.getOrganizationId() // ✅ NEW — stamp session's org onto the auto-created recording
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

        try {
            aiWorkflowExecutionService.fireTrigger(
                AiWorkflowExecutionService.TRIGGER_SESSION_ENDED,
                saved.getId(),
                saved.getTrainerEmail()
            );
        } catch (Exception e) {
            System.err.println("[LiveSessionService] Failed to fire session-ended trigger: " + e.getMessage());
        }

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
        ZoneId zone = ZoneId.of(
        	    session.getTimezone() != null ? session.getTimezone() : "UTC"
        	);
        	LocalDateTime now = LocalDateTime.now(zone);
        long diffMinutes = ChronoUnit.MINUTES.between(now, scheduledAt);

        if (session.getCreatedAt() != null) {
            long gapFromCreation = ChronoUnit.MINUTES.between(
                session.getCreatedAt(), scheduledAt
            );
            if (gapFromCreation < 15) {
                return diffMinutes <= 0;
            }
        }

        return diffMinutes <= 15;
    }

    // ─────────────────────────────────────────────────────────────────
    // IMMEDIATE NOTIFICATION FOR SHORT-SCHEDULED SESSIONS (BUG 3 FIX)
    // ─────────────────────────────────────────────────────────────────

    private void sendImmediateNotificationIfNeeded(LiveSession session) {
        if (session.getScheduledDate() == null || session.getScheduledTime() == null) return;
        if (Boolean.FALSE.equals(session.getNotifyStudents())) return;

        ZoneId zone = ZoneId.of(
            session.getTimezone() != null ? session.getTimezone() : "UTC"
        );
        LocalDateTime scheduledAt = LocalDateTime.of(
            session.getScheduledDate(), session.getScheduledTime()
        );
        long minutesAway = ChronoUnit.MINUTES.between(LocalDateTime.now(zone), scheduledAt);

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
                    "STUDENT_SESSION_CREATED",
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
        if (session.getBatchId() == null) return;

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
                    "STUDENT_SESSION_LIVE_NOW",
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

    public List<LiveSession> getBatchSessions(Long batchId, Long callerOrgId) {
        return repository.findByBatchIdForOrg(batchId, callerOrgId);
    }

    public List<LiveSession> getLiveSessions(Long batchId, Long callerOrgId) {
        return repository.findByBatchIdAndStatusForOrg(batchId, "LIVE", callerOrgId);
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
    
    public Map<String, String> resolveMeetingLink(Long id) {
        LiveSession session = getSessionById(id);
        Map<String, String> result = new java.util.HashMap<>();

        if ("EXTERNAL".equals(session.getMeetingType())
                && session.getExternalMeetingUrl() != null
                && !session.getExternalMeetingUrl().isBlank()) {
            result.put("type", "EXTERNAL");
            result.put("url", session.getExternalMeetingUrl());
        } else {
            result.put("type", "CUSTOM");
            result.put("url", urlBuilderService.generateTrainerLiveLink(session.getId()));
        }
        return result;
    }

    public List<LiveSession> getTrainerCalendar(
            String trainerEmail, LocalDate from, LocalDate to) {
        return repository.findByTrainerEmailAndScheduledDateBetween(
            trainerEmail, from, to);
    }
    public List<LiveSession> getPublishedSessions() {
        return repository.findByIsPublishedTrueAndStatusIn(
            List.of("SCHEDULED", "LIVE"));
    }


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
      return session;
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
        return repository.save(session);
    }

    String egressIdToStop = session.getEgressId();

    livekit.LivekitEgress.EgressInfo info = egressService.stopRecordingAndGetInfo(egressIdToStop);

    if (info == null) {
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

    String realFilename = info.getFileResults(0).getFilename();
    String s3Url = "https://" + bucket + ".s3." + awsRegion + ".amazonaws.com/" + realFilename;

    int partNumber = recordingService.getBySession(id, null).size() + 1; // ✅ CHANGED — see note below

    recordingService.createAutoRecordPlaceholder(
        id,
        session.getBatchId(),
        session.getTrainerEmail(),
        session.getTitle() + " — Part " + partNumber,
        s3Url,
        session.getOrganizationId() // ✅ NEW
    );

    repository.atomicClearEgressId(id, egressIdToStop);
    LiveSession freshSession = repository.findById(id)
        .orElseThrow(() -> new RuntimeException("Session not found: " + id));
    freshSession.setAutoRecord(false);
    return repository.save(freshSession);
}

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

    LiveSession fresh = repository.findById(id)

        .orElseThrow(() -> new RuntimeException("Session not found: " + id));

    fresh.setCurrentEgressFileSuffix(result.fileSuffix);

    return repository.save(fresh);

}


}