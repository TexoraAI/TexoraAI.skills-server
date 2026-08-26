package com.lms.live_session.repository;

import com.lms.live_session.entity.Meeting;
import com.lms.live_session.entity.MeetingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    Optional<Meeting> findByJoinCode(String joinCode);

    boolean existsByJoinCode(String joinCode);

    List<Meeting> findByCreatorIdOrderByCreatedAtDesc(String creatorId);

    List<Meeting> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId);

    List<Meeting> findByMeetingStatus(MeetingStatus status);

    // Used by the scheduler to flip SCHEDULED -> ACTIVE once due.
    List<Meeting> findByMeetingStatusAndScheduledTimeUtcLessThanEqual(
            MeetingStatus status, LocalDateTime now);
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE Meeting m SET m.egressId = :claimToken " +
           "WHERE m.id = :id AND m.egressId IS NULL")
    int atomicClaimEgressSlot(@Param("id") Long id, @Param("claimToken") String claimToken);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE Meeting m SET m.egressId = :newEgressId " +
           "WHERE m.id = :id AND m.egressId = :claimToken")
    int atomicFinalizeEgressId(@Param("id") Long id,
                               @Param("claimToken") String claimToken,
                               @Param("newEgressId") String newEgressId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE Meeting m SET m.egressId = NULL " +
           "WHERE m.id = :id AND m.egressId = :expectedEgressId")
    int atomicClearEgressId(@Param("id") Long id, @Param("expectedEgressId") String expectedEgressId);
    List<Meeting> findByCreatorIdAndPermanentTrueOrderByCreatedAtDesc(String creatorId);
    
    List<Meeting> findByMeetingStatusAndScheduledEndTimeUtcLessThanEqual(MeetingStatus status, LocalDateTime time);
}