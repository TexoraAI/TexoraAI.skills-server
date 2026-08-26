package com.lms.live_session.service;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Hourly trigger that syncs every CONNECTED calendar.
 *
 * Note: @EnableScheduling only needs to be declared once per application context.
 * If your @SpringBootApplication class (or another config class) already has it,
 * this one is redundant but harmless - Spring just registers the scheduling
 * infrastructure once either way.
 */
@Service
@EnableScheduling
public class CalendarSyncScheduler {

    private final GoogleCalendarService googleCalendarService;

    public CalendarSyncScheduler(GoogleCalendarService googleCalendarService) {
        this.googleCalendarService = googleCalendarService;
    }

    @Scheduled(fixedRate = 3600000) // every 1 hour
    public void syncAllConnectedCalendars() {
        try {
            googleCalendarService.scheduledSyncAll();
        } catch (Exception e) {
            System.err.println("❌ CalendarSyncScheduler.syncAllConnectedCalendars failed: " + e.getMessage());
        }
    }
}