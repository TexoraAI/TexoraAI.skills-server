package com.lms.live_session.repository;

import com.lms.live_session.entity.Reminder;
import com.lms.live_session.entity.ReminderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Note: reminderTime/status are mapped as enum types (ReminderTime,
 * ReminderStatus) on the Reminder entity rather than raw String, so the
 * derived-query parameters below use those enum types instead of String.
 * This keeps the JPQL binding correct while the DTOs still expose plain
 * Strings at the API boundary.
 */
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    List<Reminder> findByCreatorIdAndStatusOrderByCreatedAtAsc(String creatorId, ReminderStatus status);

    // Added beyond the original spec: getMyReminders() needs all of a
    // creator's reminders (not filtered by status), sorted by createdAt.
    List<Reminder> findByCreatorIdOrderByCreatedAtAsc(String creatorId);

    List<Reminder> findByStatus(ReminderStatus status);

    List<Reminder> findByEventId(Long eventId);

    List<Reminder> findByScheduleId(Long scheduleId);

    void deleteByEventId(Long eventId);

    void deleteByScheduleId(Long scheduleId);
}