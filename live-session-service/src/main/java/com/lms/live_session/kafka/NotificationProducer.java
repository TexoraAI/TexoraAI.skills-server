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
}