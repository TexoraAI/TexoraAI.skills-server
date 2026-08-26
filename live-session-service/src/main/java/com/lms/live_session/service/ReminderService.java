package com.lms.live_session.service;

import com.lms.live_session.dto.ReminderRequestDTO;
import com.lms.live_session.dto.ReminderResponseDTO;
import com.lms.live_session.entity.Reminder;
import com.lms.live_session.entity.ReminderStatus;
import com.lms.live_session.entity.ReminderTime;
import com.lms.live_session.event.SessionNotificationEvent;
import com.lms.live_session.kafka.NotificationProducer;
import com.lms.live_session.repository.EventRepository;
import com.lms.live_session.repository.ReminderRepository;
import com.lms.live_session.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final EventRepository eventRepository;
    private final ScheduleRepository scheduleRepository;
    private final NotificationProducer notificationProducer;

    @Autowired
    public ReminderService(ReminderRepository reminderRepository,
                            EventRepository eventRepository,
                            ScheduleRepository scheduleRepository,
                            NotificationProducer notificationProducer) {
        this.reminderRepository = reminderRepository;
        this.eventRepository = eventRepository;
        this.scheduleRepository = scheduleRepository;
        this.notificationProducer = notificationProducer;
    }

    public ReminderResponseDTO createReminder(ReminderRequestDTO dto, String creatorId) {
        if (dto.getEventId() == null && dto.getScheduleId() == null) {
            throw new IllegalArgumentException("Either eventId or scheduleId is required");
        }

        Reminder reminder = new Reminder();
        reminder.setEventId(dto.getEventId());
        reminder.setScheduleId(dto.getScheduleId());
        reminder.setReminderTime(ReminderTime.fromValue(dto.getReminderTime()));
        reminder.setStatus(ReminderStatus.PENDING);
        reminder.setCreatorId(creatorId);

        Reminder saved = reminderRepository.save(reminder);
        return mapToDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<ReminderResponseDTO> getMyReminders(String creatorId) {
        List<Reminder> reminders = reminderRepository.findByCreatorIdOrderByCreatedAtAsc(creatorId);
        return reminders.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReminderResponseDTO getReminderById(Long reminderId, String creatorId) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new IllegalArgumentException("Reminder not found"));

        verifyOwnership(reminder, creatorId);

        return mapToDTO(reminder);
    }

    public ReminderResponseDTO updateReminder(Long reminderId, ReminderRequestDTO dto, String creatorId) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new IllegalArgumentException("Reminder not found"));

        verifyOwnership(reminder, creatorId);

        reminder.setReminderTime(ReminderTime.fromValue(dto.getReminderTime()));

        Reminder saved = reminderRepository.save(reminder);
        return mapToDTO(saved);
    }

    public void deleteReminder(Long reminderId, String creatorId) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new IllegalArgumentException("Reminder not found"));

        verifyOwnership(reminder, creatorId);

        reminderRepository.delete(reminder);
    }

    public void dismissReminder(Long reminderId, String creatorId) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new IllegalArgumentException("Reminder not found"));

        verifyOwnership(reminder, creatorId);

        reminder.setStatus(ReminderStatus.DISMISSED);
        reminderRepository.save(reminder);
    }

    /**
     * Called by ReminderScheduler on a fixed interval. Scans all PENDING
     * reminders, and for any whose target send time has passed, fires a
     * notification and marks it SENT.
     *
     * ASSUMPTION: this assumes an Event entity exposes getStartTime()
     * (LocalDateTime) and a Schedule entity exposes getScheduledTime()
     * (LocalDateTime). Adjust the two getter calls below to match your
     * actual Event/Schedule entity field names if they differ.
     */
    public void processDueReminders() {
        List<Reminder> pending = reminderRepository.findByStatus(ReminderStatus.PENDING);
        LocalDateTime now = LocalDateTime.now();

        for (Reminder reminder : pending) {
            LocalDateTime scheduledTime = resolveScheduledTime(reminder);
            if (scheduledTime == null) {
                // Associated event/schedule no longer exists; skip it.
                continue;
            }

            if (reminder.getReminderTime() == ReminderTime.NO_REMINDER) {
                continue;
            }

            LocalDateTime sendTime = scheduledTime.minusMinutes(reminder.getReminderTime().getMinutesBefore());

            if (!now.isBefore(sendTime)) {
                sendNotification(reminder);
                reminder.setStatus(ReminderStatus.SENT);
                reminder.setSentAt(now);
                reminderRepository.save(reminder);
            }
        }
    }

    /**
     * ASSUMPTION FIXED: Event and Schedule store date and start time as
     * separate fields (LocalDate date, LocalTime startTime) rather than a
     * single LocalDateTime, and neither has a getScheduledTime() method.
     * Combine them here instead.
     */
    private LocalDateTime resolveScheduledTime(Reminder reminder) {
        if (reminder.getEventId() != null) {
            return eventRepository.findById(reminder.getEventId())
                    .filter(event -> event.getDate() != null && event.getStartTime() != null)
                    .map(event -> LocalDateTime.of(event.getDate(), event.getStartTime()))
                    .orElse(null);
        }
        if (reminder.getScheduleId() != null) {
            return scheduleRepository.findById(reminder.getScheduleId())
                    .filter(schedule -> schedule.getDate() != null && schedule.getStartTime() != null)
                    .map(schedule -> LocalDateTime.of(schedule.getDate(), schedule.getStartTime()))
                    .orElse(null);
        }
        return null;
    }

    /**
     * Builds a SessionNotificationEvent from the reminder and publishes it
     * via NotificationProducer.sendReminderDue(), same pattern as the
     * workflow-triggered notifications already in this codebase.
     *
     * recipientEmail is set from reminder.getCreatorId() — matches how
     * auth.getName() (the email) is used as creatorId elsewhere. sessionLink
     * is left null: Event only carries a meetingId (FK to Meeting), and
     * Schedule has no online-meeting concept at all, so there's no join URL
     * to resolve without also looking up the Meeting entity. If you want the
     * link included, tell me how to fetch a Meeting's join URL and I'll wire
     * it in.
     */
    private void sendNotification(Reminder reminder) {
        try {
            String sessionTitle = null;
            java.time.LocalDate scheduledDate = null;
            java.time.LocalTime scheduledStartTime = null;

            if (reminder.getEventId() != null) {
                var event = eventRepository.findById(reminder.getEventId()).orElse(null);
                if (event != null) {
                    sessionTitle = event.getTitle();
                    scheduledDate = event.getDate();
                    scheduledStartTime = event.getStartTime();
                }
            } else if (reminder.getScheduleId() != null) {
                var schedule = scheduleRepository.findById(reminder.getScheduleId()).orElse(null);
                if (schedule != null) {
                    sessionTitle = schedule.getTitle();
                    scheduledDate = schedule.getDate();
                    scheduledStartTime = schedule.getStartTime();
                }
            }

            SessionNotificationEvent event = new SessionNotificationEvent(
                    reminder.getEventId(),
                    null,                                                    // trainerEmail — not applicable to a generic reminder
                    null,                                                    // batchId — not applicable
                    sessionTitle,
                    scheduledDate == null ? null : scheduledDate.toString(), // "2026-05-10"
                    scheduledStartTime == null ? null : scheduledStartTime.toString(), // "14:30"
                    null,                                                    // durationMinutes — could derive from endTime - startTime if needed
                    "REMINDER_DUE",                                         // overwritten again inside sendReminderDue(), harmless here
                    reminder.getCreatorId(),                                // recipientEmail
                    null,                                                    // recipientName
                    null,                                                    // recipientRole
                    null                                                     // sessionLink — see method comment above
            );

            notificationProducer.sendReminderDue(event);
        } catch (Exception e) {
            System.err.println("Failed to publish reminder-due event for reminderId=" + reminder.getId()
                    + ": " + e.getMessage());
        }
    }

    private void verifyOwnership(Reminder reminder, String creatorId) {
        if (!reminder.getCreatorId().equals(creatorId)) {
            throw new IllegalArgumentException("You do not have permission to access this reminder");
        }
    }

    private ReminderResponseDTO mapToDTO(Reminder reminder) {
        ReminderResponseDTO dto = new ReminderResponseDTO(
                reminder.getId(),
                reminder.getEventId(),
                reminder.getScheduleId(),
                reminder.getReminderTime() == null ? null : reminder.getReminderTime().getValue(),
                reminder.getStatus() == null ? null : reminder.getStatus().name(),
                reminder.getCreatedAt(),
                reminder.getSentAt()
        );

        if (reminder.getEventId() != null) {
            eventRepository.findById(reminder.getEventId()).ifPresent(event -> {
                dto.setLinkedTitle(event.getTitle());
                dto.setLinkedDate(event.getDate() == null ? null : event.getDate().toString());
                dto.setLinkedStartTime(event.getStartTime() == null ? null : event.getStartTime().toString());
            });
        } else if (reminder.getScheduleId() != null) {
            scheduleRepository.findById(reminder.getScheduleId()).ifPresent(schedule -> {
                dto.setLinkedTitle(schedule.getTitle());
                dto.setLinkedDate(schedule.getDate() == null ? null : schedule.getDate().toString());
                dto.setLinkedStartTime(schedule.getStartTime() == null ? null : schedule.getStartTime().toString());
            });
        }

        return dto;
    }
    public void deleteRemindersByEventId(Long eventId) {
        reminderRepository.deleteByEventId(eventId);
    }
    
    public void deleteRemindersByScheduleId(Long scheduleId) {
        reminderRepository.deleteByScheduleId(scheduleId);
    }
}