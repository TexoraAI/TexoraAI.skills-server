package com.lms.live_session.repository;

import com.lms.live_session.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByCreatorIdOrderByCreatedAtDesc(String creatorId);

    List<Event> findByCreatorIdAndDateBetween(String creatorId, LocalDate startDate, LocalDate endDate);

    List<Event> findByMeetingIdOrderByCreatedAtDesc(Long meetingId);

    boolean existsByMeetingId(Long meetingId);

    // Added for Google Calendar sync idempotency: lets GoogleCalendarService tell
    // whether an incoming Google event was already imported, so re-syncing updates
    // the existing row instead of creating a duplicate.
    Optional<Event> findByGoogleEventId(String googleEventId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Event e WHERE e.creatorId = :creatorId")
    void deleteByCreatorId(String creatorId);
}