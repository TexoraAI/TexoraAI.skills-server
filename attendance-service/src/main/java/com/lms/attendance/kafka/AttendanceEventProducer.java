package com.lms.attendance.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.lms.attendance.event.AttendanceMarkedEvent;

@Component
public class AttendanceEventProducer {

    private static final String TOPIC = "attendance-events";

    private final KafkaTemplate<String, AttendanceMarkedEvent> kafkaTemplate;

    public AttendanceEventProducer(
            KafkaTemplate<String, AttendanceMarkedEvent> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(AttendanceMarkedEvent event) {
        kafkaTemplate.send(
                TOPIC,
                event.getBatchId().toString(),
                event
        );
    }
}
