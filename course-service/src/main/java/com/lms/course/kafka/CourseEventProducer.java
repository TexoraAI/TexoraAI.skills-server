package com.lms.course.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.course.dto.CourseEvent;
import com.lms.course.event.CourseLifecycleEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class CourseEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${topics.course}")
    private String topic;

    public CourseEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // ================= EXISTING COURSE EVENTS =================
    public void send(CourseEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, json);
            System.out.println("✔ Sent COURSE Event → " + json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send course event", e);
        }
    }

    // ================= COURSE DELETE =================
    public void publishCourseDeleted(Long id) {

        try {
            CourseLifecycleEvent event = new CourseLifecycleEvent();
            event.setType("COURSE_DELETED");
            event.setCourseId(id);

            String json = objectMapper.writeValueAsString(event);

            kafkaTemplate.send("course-lifecycle", json);
            kafkaTemplate.flush();

            System.out.println("📤 Sent COURSE Event → " + json);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
 // ================= COURSE UPDATED =================
    public void publishCourseUpdated(Long courseId) {
        try {

            CourseLifecycleEvent event =
                    new CourseLifecycleEvent("COURSE_UPDATED", courseId);

            String json = objectMapper.writeValueAsString(event);

            kafkaTemplate.send("course-lifecycle", json);

            System.out.println("📤 COURSE_UPDATED sent → " + json);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send course update event", e);
        }
    }
}