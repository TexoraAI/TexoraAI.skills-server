package com.lms.course.kafka;

import com.lms.course.dto.ContentEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ContentEventProducer {

    // ✅ Object not String — same as batch producer
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${topics.content}")
    private String topic;

    public ContentEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // ✅ send ContentEvent object directly — no ObjectMapper
    public void sendEvent(ContentEvent event) {
        kafkaTemplate.send(topic, event);
        System.out.println("📤 Sent CONTENT Event → " + event);
    }
}