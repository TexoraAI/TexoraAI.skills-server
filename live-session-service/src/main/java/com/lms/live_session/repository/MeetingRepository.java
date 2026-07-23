package com.lms.live_session.repository;

import com.lms.live_session.entity.Meeting;
import com.lms.live_session.entity.MeetingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    Optional<Meeting> findByJoinCode(String joinCode);

    boolean existsByJoinCode(String joinCode);

    List<Meeting> findByCreatorIdOrderByCreatedAtDesc(String creatorId);

    List<Meeting> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId);

    List<Meeting> findByMeetingStatus(MeetingStatus status);

    // Used by the scheduler to flip SCHEDULED -> ACTIVE once due.
    List<Meeting> findByMeetingStatusAndScheduledTimeUtcLessThanEqual(
            MeetingStatus status, LocalDateTime now);
}