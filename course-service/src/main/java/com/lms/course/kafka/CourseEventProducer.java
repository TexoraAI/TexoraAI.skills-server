
package com.lms.course.kafka;

import com.lms.course.dto.CourseEvent;
import com.lms.course.event.CourseLifecycleEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CourseEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${topics.course}")
    private String topic;

    public CourseEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // ── used by CourseService.create() ───────────────────────────────────────
    public void send(CourseEvent event) {
        kafkaTemplate.send(topic, event);
        System.out.println("✔ Sent COURSE Event → " + event);
    }

    // ── used by CourseService.delete() ───────────────────────────────────────
    // Fires on "course-lifecycle" topic.
    // video-service + file-service both listen here to clean ALL files for the course.
    public void publishCourseDeleted(Long courseId) {
        CourseLifecycleEvent event = new CourseLifecycleEvent();
        event.setType("COURSE_DELETED");
        event.setCourseId(courseId);
        kafkaTemplate.send("course-lifecycle", event);
        kafkaTemplate.flush();
        System.out.println("📤 Sent COURSE_DELETED → courseId=" + courseId);
    }

    // ── used by CourseService.update() ───────────────────────────────────────
    public void publishCourseUpdated(Long courseId) {
        CourseLifecycleEvent event = new CourseLifecycleEvent("COURSE_UPDATED", courseId);
        kafkaTemplate.send("course-lifecycle", event);
        System.out.println("📤 Sent COURSE_UPDATED → courseId=" + courseId);
    }
}