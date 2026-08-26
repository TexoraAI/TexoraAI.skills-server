package com.lms.live_session.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
public class ReminderScheduler {

    private final ReminderService reminderService;

    @Autowired
    public ReminderScheduler(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @Scheduled(fixedRate = 60000) // Every 1 minute
    public void checkAndSendReminders() {
        try {
            reminderService.processDueReminders();
        } catch (Exception e) {
            System.err.println("❌ ReminderScheduler.checkAndSendReminders failed: " + e.getMessage());
        }
    }
}