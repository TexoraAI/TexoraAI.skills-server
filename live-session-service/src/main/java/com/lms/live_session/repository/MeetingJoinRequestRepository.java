package com.lms.live_session.repository;

import com.lms.live_session.entity.JoinRequestStatus;
import com.lms.live_session.entity.MeetingJoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeetingJoinRequestRepository extends JpaRepository<MeetingJoinRequest, Long> {

    Optional<MeetingJoinRequest> findByIdAndMeetingId(Long id, Long meetingId);

    Optional<MeetingJoinRequest> findByIdAndMeetingIdAndGuestIdentity(Long id, Long meetingId, String guestIdentity);

    List<MeetingJoinRequest> findByMeetingIdAndStatusOrderByRequestedAtAsc(Long meetingId, JoinRequestStatus status);
    
    List<MeetingJoinRequest> findByMeetingIdOrderByRequestedAtDesc(Long meetingId);
    
 // ADD — used when a meeting is deleted, to clean up its join requests too
    void deleteByMeetingId(Long meetingId);
}