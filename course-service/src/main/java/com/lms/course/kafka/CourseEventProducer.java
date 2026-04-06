package com.lms.course.kafka;

import com.lms.course.dto.CourseEvent;
import com.lms.course.event.CourseLifecycleEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class CourseEventProducer {

    // ✅ Object not String — same as batch producer
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${topics.course}")
    private String topic;

    public CourseEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // ✅ send CourseEvent object directly — no ObjectMapper
    public void send(CourseEvent event) {
        kafkaTemplate.send(topic, event);
        System.out.println("✔ Sent COURSE Event → " + event);
    }

    public void publishCourseDeleted(Long id) {
        CourseLifecycleEvent event = new CourseLifecycleEvent();
        event.setType("COURSE_DELETED");
        event.setCourseId(id);
        kafkaTemplate.send("course-lifecycle", event); // ✅ send object directly
        kafkaTemplate.flush();
        System.out.println("📤 Sent COURSE_DELETED Event → courseId=" + id);
    }

    public void publishCourseUpdated(Long courseId) {
        CourseLifecycleEvent event = new CourseLifecycleEvent("COURSE_UPDATED", courseId);
        kafkaTemplate.send("course-lifecycle", event); // ✅ send object directly
        System.out.println("📤 COURSE_UPDATED sent → courseId=" + courseId);
    }
}