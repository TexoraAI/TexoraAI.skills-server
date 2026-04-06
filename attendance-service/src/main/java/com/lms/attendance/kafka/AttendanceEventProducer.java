package com.lms.attendance.kafka;

import com.lms.attendance.event.AttendanceMarkedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class AttendanceEventProducer {

    // ✅ Object not String — same as batch producer
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public AttendanceEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        // ✅ No ObjectMapper needed — JsonSerializer handles it
    }

    public void publish(AttendanceMarkedEvent event) {
        // ✅ No ObjectMapper — send Map directly
        Map<String, Object> payload = new HashMap<>();
        payload.put("batchId", event.getBatchId());
        payload.put("studentUserId", event.getStudentUserId());
        payload.put("studentEmail", event.getStudentEmail());
        payload.put("status", event.getStatus());
        payload.put("date", event.getDate().toString());
        payload.put("trainerEmail", event.getTrainerEmail());

        Map<String, Object> kafkaEvent = new HashMap<>();
        kafkaEvent.put("type", "ATTENDANCE_MARKED");
        kafkaEvent.put("payload", payload);

        kafkaTemplate.send("attendance-events", kafkaEvent); // ✅ send object directly
        System.out.println("🔥 ATTENDANCE EVENT -> ATTENDANCE_MARKED | student=" + event.getStudentEmail());
    }
}