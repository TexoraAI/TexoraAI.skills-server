
package com.lms.live_session.kafka;

import com.lms.live_session.event.ComposedEmailEvent;
import com.lms.live_session.event.SessionNotificationEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public NotificationProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // ✅ Produce event for Student 15-min reminder
    public void sendStudentReminder(SessionNotificationEvent event) {
        event.setEventType("STUDENT_REMINDER_15MIN");
        kafkaTemplate.send("session-notifications", event);
        System.out.println("✅ Student reminder event sent to Kafka: " + event.getRecipientEmail());
    }

    // ✅ Produce event for Trainer 15-min reminder
    public void sendTrainerReminder(SessionNotificationEvent event) {
        event.setEventType("TRAINER_REMINDER_15MIN");
        kafkaTemplate.send("session-notifications", event);
        System.out.println("✅ Trainer reminder event sent to Kafka: " + event.getRecipientEmail());
    }

    // ✅ Produce event for Public User booking confirmation
    public void sendPublicUserBooking(SessionNotificationEvent event) {
        event.setEventType("PUBLIC_BOOKING_CONFIRMATION");
        kafkaTemplate.send("session-notifications", event);
        System.out.println("✅ Public user booking event sent to Kafka: " + event.getRecipientEmail());
    }

    // ✅ Produce event for Public User 15-min reminder (WhatsApp/SMS)
    public void sendPublicUserReminder(SessionNotificationEvent event) {
        event.setEventType("PUBLIC_REMINDER_15MIN");
        kafkaTemplate.send("session-notifications", event);
        System.out.println("✅ Public user reminder event sent to Kafka: " + event.getRecipientEmail());
    }

    // ─────────────────────────────────────────────────────────────────
    // NEW — Phase 4 workflow-triggered events (additive, does not touch
    // any existing method or eventType above). Reuses the same
    // "session-notifications" topic and SessionNotificationEvent class;
    // the notification microservice's consumer must be updated to
    // recognize these two new eventType values to actually act on them
    // (send an email / fan out to students). Until then, these events
    // will be published but likely ignored by the current consumer if
    // it only switches on the pre-existing eventType values.
    // ─────────────────────────────────────────────────────────────────

    // ✅ Produce event: workflow action node "Send email to trainer"
    public void sendWorkflowTrainerEmail(SessionNotificationEvent event) {
        event.setEventType("WORKFLOW_TRAINER_EMAIL");
        kafkaTemplate.send("session-notifications", event);
        System.out.println("✅ Workflow trainer-email event sent to Kafka: " + event.getRecipientEmail());
    }

    // ✅ Produce event: workflow action node "Notify students"
    public void sendWorkflowNotifyStudents(SessionNotificationEvent event) {
        event.setEventType("WORKFLOW_NOTIFY_STUDENTS");
        kafkaTemplate.send("session-notifications", event);
        System.out.println("✅ Workflow notify-students event sent to Kafka for session: " + event.getSessionId());
    }

    // ─────────────────────────────────────────────────────────────────
    // NEW — Reminder System integration (additive). Fired by
    // ReminderService.processDueReminders() when a PENDING reminder's
    // fire time has passed. Reuses "session-notifications" +
    // SessionNotificationEvent, same as the workflow methods above.
    // The notification-service consumer must add a case for
    // "REMINDER_DUE" to actually act on it (send email/push) — until
    // then this publishes but is likely ignored downstream.
    // ─────────────────────────────────────────────────────────────────

    // ✅ Produce event: a scheduled reminder is due
    public void sendReminderDue(SessionNotificationEvent event) {
        event.setEventType("REMINDER_DUE");
        kafkaTemplate.send("session-notifications", event);
        System.out.println("✅ Reminder-due event sent to Kafka: " + event.getRecipientEmail());
    }

    // ─────────────────────────────────────────────────────────────────
    // NEW — Email Management System integration (additive). Fired by
    // EmailService.sendEmail() when a user sends a composed email. Uses
    // a NEW topic ("composed-email-events") and a NEW event class
    // (ComposedEmailEvent) rather than session-notifications /
    // SessionNotificationEvent, since a composed email has no
    // session/batch/schedule concept and carries multiple recipients
    // (to/cc/bcc) rather than a single recipientEmail. The
    // notification-service needs a NEW @KafkaListener for this topic —
    // it won't be picked up by the existing session-notifications
    // consumer.
    // ─────────────────────────────────────────────────────────────────

    // ✅ Produce event: a composed email was sent
    public void sendComposedEmail(ComposedEmailEvent event) {
        kafkaTemplate.send("composed-email-events", event);
        System.out.println("✅ Composed email event sent to Kafka: " + event.getFromEmail());
    }
 // ✅ Produce event: attendee invited to an Event (required/optional)
    public void sendMeetingInvite(SessionNotificationEvent event) {
        event.setEventType("MEETING_INVITE");
        kafkaTemplate.send("meeting-invite-notifications", event);
        System.out.println("✅ Meeting invite event sent to Kafka: " + event.getRecipientEmail());
    }
}