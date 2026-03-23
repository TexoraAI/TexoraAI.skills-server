package com.lms.live_session.kafka;

import com.lms.live_session.event.LiveSessionEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SessionEventProducer {

    private final KafkaTemplate<String, LiveSessionEvent> kafkaTemplate;

    public SessionEventProducer(KafkaTemplate<String, LiveSessionEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishSessionStarted(Long sessionId) {

        LiveSessionEvent event = new LiveSessionEvent();
        event.setSessionId(sessionId);
        event.setStatus("SESSION_STARTED");
        
        event.setTimestamp(LocalDateTime.now().toString());
        kafkaTemplate.send("live-session-started", event);

        System.out.println("📢 Session Started Event Sent");
    }

    public void publishStudentJoined(Long sessionId, Long studentId) {

        LiveSessionEvent event = new LiveSessionEvent();
        event.setSessionId(sessionId);
        event.setTrainerId(studentId); // reuse field
        event.setStatus("STUDENT_JOINED");
        event.setTimestamp(LocalDateTime.now().toString());

        kafkaTemplate.send("student-joined-session", event);

        System.out.println("👨‍🎓 Student Joined Event Sent");
    }
}