package com.lms.analytics.service;

import com.lms.analytics.model.AnalyticsEvent;
import com.lms.analytics.repository.AnalyticsRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnalyticsService {

    private final AnalyticsRepository repository;

    public AnalyticsService(AnalyticsRepository repository) {
        this.repository = repository;
    }

    // Kafka Listener
    @KafkaListener(topics = "notifications", groupId = "analytics-group")
    public void consumeEvent(String message) {
        AnalyticsEvent event = new AnalyticsEvent(
                detectType(message),
                message,
                LocalDateTime.now()
        );
        repository.save(event);
        System.out.println("Analytics saved: " + message);
    }

    private String detectType(String message) {
        message = message.toLowerCase();
        if (message.contains("upload")) return "VIDEO_UPLOAD";
        if (message.contains("course")) return "COURSE_EVENT";
        if (message.contains("user")) return "USER_EVENT";
        return "GENERAL";
    }

    public List<AnalyticsEvent> getAllEvents() {
        return repository.findAll();
    }

    public List<AnalyticsEvent> getEventsByType(String type) {
        return repository.findByEventType(type);
    }

    public long countByType(String type) {
        return repository.findByEventType(type).size();
    }
    // hudhjkdhudud
}
