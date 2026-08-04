//package com.lms.live_session.kafka;
//
//import com.lms.live_session.event.SessionNotificationEvent;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.stereotype.Service;
//
//@Service
//public class NotificationProducer {
//
//    private final KafkaTemplate<String, Object> kafkaTemplate;
//
//    public NotificationProducer(KafkaTemplate<String, Object> kafkaTemplate) {
//        this.kafkaTemplate = kafkaTemplate;
//    }
//
//    // ✅ Produce event for Student 15-min reminder
//    public void sendStudentReminder(SessionNotificationEvent event) {
//        event.setEventType("STUDENT_REMINDER_15MIN");
//        kafkaTemplate.send("session-notifications", event);
//        System.out.println("✅ Student reminder event sent to Kafka: " + event.getRecipientEmail());
//    }
//
//    // ✅ Produce event for Trainer 15-min reminder
//    public void sendTrainerReminder(SessionNotificationEvent event) {
//        event.setEventType("TRAINER_REMINDER_15MIN");
//        kafkaTemplate.send("session-notifications", event);
//        System.out.println("✅ Trainer reminder event sent to Kafka: " + event.getRecipientEmail());
//    }
//
//    // ✅ Produce event for Public User booking confirmation
//    public void sendPublicUserBooking(SessionNotificationEvent event) {
//        event.setEventType("PUBLIC_BOOKING_CONFIRMATION");
//        kafkaTemplate.send("session-notifications", event);
//        System.out.println("✅ Public user booking event sent to Kafka: " + event.getRecipientEmail());
//    }
//
//    // ✅ Produce event for Public User 15-min reminder (WhatsApp/SMS)
//    public void sendPublicUserReminder(SessionNotificationEvent event) {
//        event.setEventType("PUBLIC_REMINDER_15MIN");
//        kafkaTemplate.send("session-notifications", event);
//        System.out.println("✅ Public user reminder event sent to Kafka: " + event.getRecipientEmail());
//    }
//}
package com.lms.live_session.kafka;

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
}