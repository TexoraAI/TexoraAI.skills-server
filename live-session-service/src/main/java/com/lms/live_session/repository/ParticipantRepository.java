package com.lms.live_session.repository;

import com.lms.live_session.entity.SessionParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ParticipantRepository extends JpaRepository<SessionParticipant, Long> {

    List<SessionParticipant> findBySessionId(Long sessionId);

}