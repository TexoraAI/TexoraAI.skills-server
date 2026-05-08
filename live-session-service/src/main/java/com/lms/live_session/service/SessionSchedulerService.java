package com.lms.live_session.service;

import com.lms.live_session.entity.LiveSession;
import com.lms.live_session.entity.PublicSessionBooking;
import com.lms.live_session.entity.StudentBatchMap;
import com.lms.live_session.event.SessionNotificationEvent;
import com.lms.live_session.kafka.NotificationProducer;
import com.lms.live_session.repository.LiveSessionRepository;
import com.lms.live_session.repository.PublicBookingRepository;
import com.lms.live_session.repository.StudentBatchMapRepository;
import com.lms.live_session.repository.TrainerBatchMapRepository;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@EnableScheduling
public class SessionSchedulerService {

    private final LiveSessionRepository sessionRepository;
    private final PublicBookingRepository publicBookingRepository;
    private final NotificationProducer notificationProducer;
    private final UrlBuilderService urlBuilderService;
    private final StudentBatchMapRepository studentBatchMapRepository;
    private final TrainerBatchMapRepository trainerBatchMapRepository;

    public SessionSchedulerService(
            LiveSessionRepository sessionRepository,
            PublicBookingRepository publicBookingRepository,
            NotificationProducer notificationProducer,
            UrlBuilderService urlBuilderService,
            StudentBatchMapRepository studentBatchMapRepository,
            TrainerBatchMapRepository trainerBatchMapRepository) {
        this.sessionRepository         = sessionRepository;
        this.publicBookingRepository   = publicBookingRepository;
        this.notificationProducer      = notificationProducer;
        this.urlBuilderService         = urlBuilderService;
        this.studentBatchMapRepository = studentBatchMapRepository;
        this.trainerBatchMapRepository = trainerBatchMapRepository;
    }

    // ─────────────────────────────────────────────────────────────────
    // AUTO-END LIVE SESSIONS
    // BUG 2 FIX: Uses actualStartTime + duration, not scheduledTime
    // Runs every 30 seconds for accuracy
    // ─────────────────────────────────────────────────────────────────

    @Scheduled(fixedRate = 30000)
    public void autoEndSessions() {
        LocalDateTime now = LocalDateTime.now();
        List<LiveSession> liveSessions = sessionRepository.findByStatus("LIVE");

        for (LiveSession session : liveSessions) {
            if (shouldEnd(session, now)) {
                try {
                    session.setStatus("ENDED");
                    session.setActualEndTime(now);
                    sessionRepository.save(session);
                    System.out.println("✅ Auto-ended: '" + session.getTitle()
                        + "' (ID: " + session.getId() + ") at " + now);
                } catch (Exception e) {
                    System.err.println("❌ Failed to auto-end session " + session.getId()
                        + ": " + e.getMessage());
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // 15-MIN NOTIFICATION TO TRAINER + STUDENTS
    // BUG 3 FIX: Narrow 1-minute window (14-15 min before) so it fires
    // exactly ONCE per session, not every minute for 15 minutes.
    // Sessions scheduled < 14 min away are handled at creation time
    // by LiveSessionService.sendImmediateNotificationIfNeeded()
    // ─────────────────────────────────────────────────────────────────

    @Scheduled(fixedRate = 60000)
    public void sendNotificationsBefore15Minutes() {
        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        List<LiveSession> sessions = sessionRepository
                .findByStatusAndScheduledDate("SCHEDULED", today);

        for (LiveSession session : sessions) {
            // ✅ BUG 3 FIX: Only notify if session is in the narrow 14-15 min window
            if (isInExact15MinWindow(session.getScheduledTime(), currentTime)) {
                sendTrainerNotification(session);
                sendStudentNotifications(session);
                System.out.println("✅ 15-min notification sent for: '"
                    + session.getTitle() + "' at " + currentTime);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // 15-MIN REMINDER TO PUBLIC BOOKINGS
    // Same narrow window fix applied
    // ─────────────────────────────────────────────────────────────────

    @Scheduled(fixedRate = 60000)
    public void sendPublicUserReminders() {
        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        List<LiveSession> sessions =
                sessionRepository.findByStatusAndScheduledDate("SCHEDULED", today);

        for (LiveSession session : sessions) {
            if (isInExact15MinWindow(session.getScheduledTime(), currentTime)) {

                List<PublicSessionBooking> bookings =
                        publicBookingRepository.findBySessionIdAndBookingStatus(
                                session.getId(), "ACTIVE");

                for (PublicSessionBooking booking : bookings) {
                    try {
                        String joinLink = urlBuilderService.generatePublicJoinLink(
                                booking.getUniqueAccessToken());

                        SessionNotificationEvent event = new SessionNotificationEvent(
                                session.getId(),
                                session.getTrainerEmail(),
                                session.getBatchId(),
                                session.getTitle(),
                                session.getScheduledDate().toString(),
                                session.getScheduledTime().toString(),
                                session.getDuration(),
                                "PUBLIC_REMINDER_15MIN",
                                booking.getEmail(),
                                booking.getFullName(),
                                "PUBLIC_USER",
                                joinLink
                        );
                        notificationProducer.sendPublicUserReminder(event);

                    } catch (Exception e) {
                        System.err.println("❌ Public reminder failed for booking "
                            + booking.getId() + ": " + e.getMessage());
                    }
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // HELPER: shouldEnd
    // BUG 2 FIX: Prefer actualStartTime over scheduledTime
    // ─────────────────────────────────────────────────────────────────

    private boolean shouldEnd(LiveSession session, LocalDateTime now) {
        // ✅ BUG 2 FIX: Use actual start time if available
        LocalDateTime startRef = session.getActualStartTime();

        // Fallback: use scheduledDate + scheduledTime
        if (startRef == null) {
            if (session.getScheduledDate() == null || session.getScheduledTime() == null) {
                System.err.println("⚠️ Session " + session.getId()
                    + " has no start time reference — cannot auto-end");
                return false;
            }
            startRef = LocalDateTime.of(
                session.getScheduledDate(),
                session.getScheduledTime()
            );
        }

        int durationMinutes = session.getDuration() != null ? session.getDuration() : 60;
        LocalDateTime sessionEnd = startRef.plusMinutes(durationMinutes);

        boolean isOver = now.isAfter(sessionEnd) || now.isEqual(sessionEnd);
        if (isOver) {
            System.out.println("⏰ Session '" + session.getTitle()
                + "' started: " + startRef
                + " | duration: " + durationMinutes + " min"
                + " | ends: " + sessionEnd
                + " | now: " + now
                + " → ENDING");
        }
        return isOver;
    }

    // ─────────────────────────────────────────────────────────────────
    // HELPER: isInExact15MinWindow
    // BUG 3 FIX: Narrow window of 14:00-15:00 min before session.
    // Fires ONCE per session (scheduler runs every 60s).
    // Does NOT fire for sessions scheduled < 14 min away.
    // ─────────────────────────────────────────────────────────────────

    private boolean isInExact15MinWindow(LocalTime scheduledTime, LocalTime currentTime) {
        if (scheduledTime == null) return false;

        // Window: session starts between 14 min and 15 min from now
        LocalTime windowStart = currentTime.plusMinutes(14);
        LocalTime windowEnd   = currentTime.plusMinutes(15);

        return (scheduledTime.isAfter(windowStart) || scheduledTime.equals(windowStart))
            && (scheduledTime.isBefore(windowEnd)  || scheduledTime.equals(windowEnd));
    }

    // ─────────────────────────────────────────────────────────────────
    // HELPER: Send trainer notification
    // ─────────────────────────────────────────────────────────────────

    private void sendTrainerNotification(LiveSession session) {
        try {
            String trainerEmail = session.getTrainerEmail();
            String liveLink = urlBuilderService.generateTrainerLiveLink(session.getId());

            SessionNotificationEvent event = new SessionNotificationEvent(
                    session.getId(),
                    trainerEmail,
                    session.getBatchId(),
                    session.getTitle(),
                    session.getScheduledDate().toString(),
                    session.getScheduledTime().toString(),
                    session.getDuration(),
                    "TRAINER_REMINDER_15MIN",
                    trainerEmail,
                    trainerEmail,
                    "TRAINER",
                    liveLink
            );
            notificationProducer.sendTrainerReminder(event);
            System.out.println("✅ Trainer notified: " + trainerEmail);

        } catch (Exception e) {
            System.err.println("❌ Trainer notification failed: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // HELPER: Send student notifications
    // ─────────────────────────────────────────────────────────────────

    private void sendStudentNotifications(LiveSession session) {
        List<StudentBatchMap> students =
                studentBatchMapRepository.findByBatchId(session.getBatchId());

        for (StudentBatchMap student : students) {
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
                        "STUDENT_REMINDER_15MIN",
                        student.getStudentEmail(),
                        student.getStudentEmail(),
                        "STUDENT",
                        studentLink
                );
                notificationProducer.sendStudentReminder(event);

            } catch (Exception e) {
                System.err.println("❌ Student notification failed for "
                    + student.getStudentEmail() + ": " + e.getMessage());
            }
        }
    }
}