package com.lms.chat.repository;

import com.lms.chat.entity.MeetingSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeetingSummaryRepository extends JpaRepository<MeetingSummary, Long> {

    Optional<MeetingSummary> findByMeetingId(Long meetingId);

    List<MeetingSummary> findByRequestedByEmailOrderByCreatedAtDesc(String email);

    List<MeetingSummary> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId);
}