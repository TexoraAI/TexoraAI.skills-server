package com.lms.live_session.repository;

import com.lms.live_session.entity.WhiteboardSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WhiteboardSnapshotRepository extends JpaRepository<WhiteboardSnapshot, Long> {
    Optional<WhiteboardSnapshot> findBySessionId(Long sessionId);
}