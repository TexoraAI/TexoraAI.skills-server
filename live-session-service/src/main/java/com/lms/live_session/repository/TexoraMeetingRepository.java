package com.lms.live_session.repository;

import com.lms.live_session.entity.TexoraMeeting;
import com.lms.live_session.entity.TexoraMeetingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TexoraMeetingRepository extends JpaRepository<TexoraMeeting, Long> {
    Optional<TexoraMeeting> findByTexoraMeetingId(String texoraMeetingId);
    Optional<TexoraMeeting> findByJoinCode(String joinCode);
    boolean existsByJoinCode(String joinCode);
    List<TexoraMeeting> findByStatus(TexoraMeetingStatus status);
    
    Optional<TexoraMeeting> findByRoomName(String roomName);
}