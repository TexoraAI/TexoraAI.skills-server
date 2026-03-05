package com.lms.analytics.repository;

import com.lms.analytics.model.AnalyticsEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
//hbdbdjbc
public interface AnalyticsRepository extends JpaRepository<AnalyticsEvent, Long> {
    List<AnalyticsEvent> findByEventType(String eventType);
}
