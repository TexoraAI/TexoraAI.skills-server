package com.lms.live_session.repository;

import com.lms.live_session.entity.JoinRequestStatus;
import com.lms.live_session.entity.MeetingJoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface MeetingJoinRequestRepository extends JpaRepository<MeetingJoinRequest, Long> {

    Optional<MeetingJoinRequest> findByIdAndMeetingId(Long id, Long meetingId);

    Optional<MeetingJoinRequest> findByIdAndMeetingIdAndGuestIdentity(Long id, Long meetingId, String guestIdentity);

    List<MeetingJoinRequest> findByMeetingIdAndStatusOrderByRequestedAtAsc(Long meetingId, JoinRequestStatus status);
    
    List<MeetingJoinRequest> findByMeetingIdOrderByRequestedAtDesc(Long meetingId);
    
 // ADD — used when a meeting is deleted, to clean up its join requests too
    void deleteByMeetingId(Long meetingId);
    
 // NEW — Task A (scope b): has this email ever been ADMITTED into
    // ANY meeting created by this host?
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
           "FROM MeetingJoinRequest r JOIN Meeting m ON r.meetingId = m.id " +
           "WHERE m.creatorId = :creatorId AND r.guestEmail = :guestEmail AND r.status = :status")
    boolean existsAdmittedForHost(@Param("creatorId") String creatorId,
                                   @Param("guestEmail") String guestEmail,
                                   @Param("status") JoinRequestStatus status);
}