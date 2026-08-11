
package com.lms.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.notification.dto.NotificationDTO;
import com.lms.notification.repository.StudentBatchMapRepository;
import com.lms.notification.repository.TrainerBatchMapRepository;
import com.lms.notification.service.EmailService;
import com.lms.notification.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class LiveSessionEventConsumer {

    private final NotificationService notificationService;
    private final StudentBatchMapRepository studentBatchMapRepository;
    private final TrainerBatchMapRepository trainerBatchMapRepository;
    private final EmailService emailService;                          // ✅ NEW
    private final ObjectMapper mapper = new ObjectMapper();

    public LiveSessionEventConsumer(NotificationService notificationService,
                                    StudentBatchMapRepository studentBatchMapRepository,
                                    TrainerBatchMapRepository trainerBatchMapRepository,
                                    EmailService emailService) {     // ✅ NEW
        this.notificationService        = notificationService;
        this.studentBatchMapRepository  = studentBatchMapRepository;
        this.trainerBatchMapRepository  = trainerBatchMapRepository;
        this.emailService               = emailService;              // ✅ NEW
    }

    // ─────────────────────────────────────────────
    // 1. live-session-events — UNCHANGED
    // ─────────────────────────────────────────────
    @KafkaListener(topics = "live-session-events", groupId = "notification-service-group")
    public void onLiveSessionEvent(String message) {
        try {
            Map<String, Object> event = mapper.readValue(message, Map.class);

            String sessionTitle = (String) event.get("sessionTitle");
            String status       = (String) event.get("status");
            Long   batchId      = event.get("batchId") != null
                                    ? Long.valueOf(event.get("batchId").toString())
                                    : null;

            if (batchId == null) {
                System.err.println("⚠️ onLiveSessionEvent: batchId is null, skipping.");
                return;
            }

            List<String> studentEmails = studentBatchMapRepository
                    .findAllByBatchId(batchId).stream()
                    .map(m -> m.getStudentEmail()).toList();

            if (!studentEmails.isEmpty()) {
                NotificationDTO studentDto = new NotificationDTO();
                studentDto.setType("LIVE_SESSION_" + status);
                studentDto.setTitle(buildStudentTitle(status));
                studentDto.setMessage(buildStudentMessage(status, sessionTitle));
                studentDto.setTargetUserIds(studentEmails);
                notificationService.createAndPush(studentDto);
            }

            List<String> trainerEmails = trainerBatchMapRepository
                    .findAllByBatchId(batchId).stream()
                    .map(t -> t.getTrainerEmail()).toList();

            if (!trainerEmails.isEmpty()) {
                NotificationDTO trainerDto = new NotificationDTO();
                trainerDto.setType("LIVE_SESSION_TRAINER_" + status);
                trainerDto.setTitle(buildTrainerTitle(status));
                trainerDto.setMessage(buildTrainerMessage(status, sessionTitle));
                trainerDto.setTargetUserIds(trainerEmails);
                notificationService.createAndPush(trainerDto);
            }

        } catch (Exception e) {
            System.err.println("❌ LiveSessionEventConsumer [live-session-events] error: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // 2. session-notifications
    //    ✅ PUBLIC cases send email
    //    ✅ NEW: WORKFLOW_TRAINER_EMAIL / WORKFLOW_NOTIFY_STUDENTS (Phase 4)
    // ─────────────────────────────────────────────
    @KafkaListener(topics = "session-notifications", groupId = "notification-service-group")
    public void onSessionNotification(String message) {
        try {
            Map<String, Object> event = mapper.readValue(message, Map.class);

            String eventType         = (String) event.get("eventType");
            String recipientEmail    = (String) event.get("recipientEmail");
            String recipientName     = (String) event.get("recipientName");
            String sessionTitle      = (String) event.get("sessionTitle");
            String scheduledDate     = (String) event.get("scheduledDate");
            String scheduledTime     = (String) event.get("scheduledTime");
            String sessionLink       = (String) event.get("sessionLink");
            String workflowOutputText = (String) event.get("workflowOutputText"); // ✅ NEW, may be null
            Long   batchId           = event.get("batchId") != null
                                          ? Long.valueOf(event.get("batchId").toString())
                                          : null;
            Integer duration         = event.get("durationMinutes") != null
                                          ? Integer.valueOf(event.get("durationMinutes").toString())
                                          : null;

            if (eventType == null || eventType.isBlank()) {
                System.err.println("⚠️ onSessionNotification: eventType is null, skipping.");
                return;
            }

            // ✅ WORKFLOW_NOTIFY_STUDENTS is a batch-wide fan-out and does NOT
            // carry a single recipientEmail — handle it before the generic
            // recipientEmail guard below, which every other case still needs.
            if ("WORKFLOW_NOTIFY_STUDENTS".equals(eventType)) {
                handleWorkflowNotifyStudents(batchId, sessionTitle, workflowOutputText);
                return;
            }

            if (recipientEmail == null || recipientEmail.isBlank()) {
                System.err.println("⚠️ onSessionNotification: recipientEmail is null, skipping.");
                return;
            }

            NotificationDTO dto = new NotificationDTO();
            dto.setTargetUserIds(List.of(recipientEmail));

            switch (eventType) {

                case "STUDENT_REMINDER_15MIN" -> {
                    dto.setType("STUDENT_REMINDER_15MIN");
                    dto.setTitle("⏰ Session Starting Soon");
                    dto.setMessage("Your live session \"" + sessionTitle
                            + "\" starts at " + scheduledTime
                            + " on " + scheduledDate + ". Get ready!");
                }

                case "TRAINER_REMINDER_15MIN" -> {
                    dto.setType("TRAINER_REMINDER_15MIN");
                    dto.setTitle("⏰ Your Session Starts Soon");
                    dto.setMessage("You have a live session \"" + sessionTitle
                            + "\" in 15 minutes at " + scheduledTime + ". Please be ready.");
                }

                // ✅ EMAIL added for PUBLIC_BOOKING_CONFIRMATION
                case "PUBLIC_BOOKING_CONFIRMATION" -> {
                    dto.setType("PUBLIC_BOOKING_CONFIRMATION");
                    dto.setTitle("✅ Booking Confirmed");
                    dto.setMessage("Your spot for \"" + sessionTitle
                            + "\" on " + scheduledDate
                            + " at " + scheduledTime + " is confirmed!");

                    // ✅ Send confirmation email with join link
                    emailService.sendPublicBookingConfirmation(
                            recipientEmail,
                            recipientName != null ? recipientName : "there",
                            sessionTitle,
                            scheduledDate,
                            scheduledTime,
                            duration,
                            sessionLink
                    );
                }

                // ✅ EMAIL added for PUBLIC_REMINDER_15MIN
                case "PUBLIC_REMINDER_15MIN" -> {
                    dto.setType("PUBLIC_REMINDER_15MIN");
                    dto.setTitle("⏰ Session Starting in 15 Minutes");
                    dto.setMessage("\"" + sessionTitle
                            + "\" is starting soon at " + scheduledTime + ". Join now!");

                    // ✅ Send reminder email with join link
                    emailService.sendPublicSessionReminder(
                            recipientEmail,
                            recipientName != null ? recipientName : "there",
                            sessionTitle,
                            scheduledDate,
                            scheduledTime,
                            sessionLink
                    );
                }

                // ✅ NEW — Phase 4 workflow action node "Send email to trainer"
                case "WORKFLOW_TRAINER_EMAIL" -> {
                    dto.setType("WORKFLOW_TRAINER_EMAIL");
                    dto.setTitle("🤖 Workflow Notification — " + (sessionTitle != null ? sessionTitle : "Session"));
                    dto.setMessage(workflowOutputText != null && !workflowOutputText.isBlank()
                            ? truncateForInApp(workflowOutputText)
                            : "A workflow completed for \"" + sessionTitle + "\".");

                    String subject = "Workflow Update — " + (sessionTitle != null ? sessionTitle : "Your Session");
                    String htmlBody = buildWorkflowEmailHtml(sessionTitle, workflowOutputText);
                    emailService.sendAutoReplyEmail(recipientEmail, subject, htmlBody);
                }

                default -> {
                    System.err.println("⚠️ Unknown eventType: " + eventType + ", skipping.");
                    return;
                }
            }

            notificationService.createAndPush(dto);
            System.out.println("✅ [" + eventType + "] notification sent to: " + recipientEmail);

        } catch (Exception e) {
            System.err.println("❌ LiveSessionEventConsumer [session-notifications] error: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // NEW — Phase 4 workflow action node "Notify students"
    // Fans out an in-app/FCM notification to every student in the batch.
    // Mirrors the batch-lookup pattern already used in onLiveSessionEvent.
    // ─────────────────────────────────────────────
    private void handleWorkflowNotifyStudents(Long batchId, String sessionTitle, String workflowOutputText) {
        if (batchId == null) {
            System.err.println("⚠️ WORKFLOW_NOTIFY_STUDENTS: batchId is null, skipping.");
            return;
        }

        List<String> studentEmails = studentBatchMapRepository
                .findAllByBatchId(batchId).stream()
                .map(m -> m.getStudentEmail()).toList();

        if (studentEmails.isEmpty()) {
            System.err.println("⚠️ WORKFLOW_NOTIFY_STUDENTS: no students found for batchId=" + batchId);
            return;
        }

        NotificationDTO dto = new NotificationDTO();
        dto.setType("WORKFLOW_NOTIFY_STUDENTS");
        dto.setTitle("🤖 Update — " + (sessionTitle != null ? sessionTitle : "Your Session"));
        dto.setMessage(workflowOutputText != null && !workflowOutputText.isBlank()
                ? truncateForInApp(workflowOutputText)
                : "A workflow-triggered update is available for \"" + sessionTitle + "\".");
        dto.setTargetUserIds(studentEmails);

        notificationService.createAndPush(dto);
        System.out.println("✅ [WORKFLOW_NOTIFY_STUDENTS] notification fanned out to "
                + studentEmails.size() + " students for batchId=" + batchId);
    }

    // ─────────────────────────────────────────────
    // Helpers — NEW
    // ─────────────────────────────────────────────
    private String truncateForInApp(String text) {
        return text.length() > 300 ? text.substring(0, 300) + "..." : text;
    }

    private String buildWorkflowEmailHtml(String sessionTitle, String workflowOutputText) {
        String safeBody = workflowOutputText != null && !workflowOutputText.isBlank()
                ? workflowOutputText.replace("\n", "<br/>")
                : "Your workflow has completed, but produced no output text.";

        return "<html><body style=\"font-family:sans-serif;background:#f4f6fb;padding:32px;\">"
                + "<div style=\"max-width:560px;margin:0 auto;background:#ffffff;border-radius:14px;padding:28px;\">"
                + "<h2 style=\"color:#1a2340;margin-bottom:6px;\">🤖 Workflow Update</h2>"
                + "<p style=\"color:#5a6173;margin-top:0;\">Session: <strong>"
                +    (sessionTitle != null ? sessionTitle : "N/A") + "</strong></p>"
                + "<hr style=\"border:none;border-top:1px solid #e5e7eb;margin:16px 0;\"/>"
                + "<div style=\"color:#374151;line-height:1.6;\">" + safeBody + "</div>"
                + "</div></body></html>";
    }

    // ─────────────────────────────────────────────
    // Helpers — UNCHANGED
    // ─────────────────────────────────────────────
    private String buildStudentTitle(String status) {
        return switch (status) {
            case "CREATED" -> "📅 New Live Session Scheduled";
            case "STARTED" -> "🔴 Live Session Started";
            case "ENDED"   -> "✅ Live Session Ended";
            default        -> "Live Session Update";
        };
    }

    private String buildStudentMessage(String status, String sessionTitle) {
        return switch (status) {
            case "CREATED" -> "A new live session \"" + sessionTitle + "\" has been scheduled for your batch.";
            case "STARTED" -> "🔴 Live session \"" + sessionTitle + "\" has started! Join now.";
            case "ENDED"   -> "Live session \"" + sessionTitle + "\" has ended. Recording will be available soon.";
            default        -> "Live session \"" + sessionTitle + "\" status updated to: " + status;
        };
    }

    private String buildTrainerTitle(String status) {
        return switch (status) {
            case "CREATED" -> "📅 Session Scheduled";
            case "STARTED" -> "🔴 Your Session is Live";
            case "ENDED"   -> "✅ Session Ended";
            default        -> "Session Update";
        };
    }

    private String buildTrainerMessage(String status, String sessionTitle) {
        return switch (status) {
            case "CREATED" -> "Your session \"" + sessionTitle + "\" has been successfully scheduled.";
            case "STARTED" -> "🔴 Your session \"" + sessionTitle + "\" is now live!";
            case "ENDED"   -> "Your session \"" + sessionTitle + "\" has ended.";
            default        -> "Your session \"" + sessionTitle + "\" updated to: " + status;
        };
    }
 // ─────────────────────────────────────────────
 // 3. meeting-invite-notifications — NEW
 // ─────────────────────────────────────────────
 @KafkaListener(topics = "meeting-invite-notifications", groupId = "notification-service-group")
 public void onMeetingInviteEvent(String message) {
     try {
         Map<String, Object> event = mapper.readValue(message, Map.class);

         String recipientEmail = (String) event.get("recipientEmail");
         String sessionTitle   = (String) event.get("sessionTitle");
         String scheduledDate  = (String) event.get("scheduledDate");
         String scheduledTime  = (String) event.get("scheduledTime");
         String sessionLink    = (String) event.get("sessionLink");

         if (recipientEmail == null || recipientEmail.isBlank()) {
             System.err.println("⚠️ onMeetingInviteEvent: recipientEmail is null, skipping.");
             return;
         }

         NotificationDTO dto = new NotificationDTO();
         dto.setType("MEETING_INVITE");
         dto.setTitle("📅 Meeting Scheduled");
         dto.setMessage("A meeting \"" + sessionTitle + "\" has been scheduled for you at "
                 + scheduledTime + " on " + scheduledDate + ".");
         dto.setTargetUserIds(List.of(recipientEmail));
         notificationService.createAndPush(dto);

         emailService.sendMeetingInviteEmail(recipientEmail, sessionTitle, scheduledDate, scheduledTime, sessionLink);
         System.out.println("✅ [MEETING_INVITE] email sent to: " + recipientEmail);

     } catch (Exception e) {
         System.err.println("❌ LiveSessionEventConsumer [meeting-invite-notifications] error: " + e.getMessage());
     }
 }
}