package com.lms.live_session.service;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Activates SCHEDULED meetings once their scheduledTimeUtc arrives.
 * Mirrors SessionSchedulerService's pattern but is fully independent
 * of the Live Session module.
 */
@Service
@EnableScheduling
public class MeetingScheduler {

    private final MeetingService meetingService;

    public MeetingScheduler(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @Scheduled(fixedRate = 30000)
    public void activateScheduledMeetings() {
        try {
            meetingService.activateDueMeetings();
        } catch (Exception e) {
            System.err.println("❌ MeetingScheduler.activateScheduledMeetings failed: " + e.getMessage());
        }
    }
}