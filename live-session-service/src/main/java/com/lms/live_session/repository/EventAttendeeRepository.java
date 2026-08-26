package com.lms.live_session.repository;

import com.lms.live_session.entity.EventAttendee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface EventAttendeeRepository extends JpaRepository<EventAttendee, Long> {

    List<EventAttendee> findByEventId(Long eventId);

    List<EventAttendee> findByEventIdAndType(Long eventId, String type);

    List<EventAttendee> findByAttendeeEmail(String attendeeEmail);

    @Modifying
    @Transactional
    @Query("DELETE FROM EventAttendee a WHERE a.eventId = :eventId")
    void deleteByEventId(Long eventId);

    boolean existsByEventIdAndAttendeeEmail(Long eventId, String email);
    
    List<EventAttendee> findByEventIdIn(List<Long> eventIds);
}