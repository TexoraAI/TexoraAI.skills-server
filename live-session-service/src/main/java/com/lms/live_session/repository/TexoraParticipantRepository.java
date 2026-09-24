package com.lms.live_session.repository;

import com.lms.live_session.entity.TexoraParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TexoraParticipantRepository extends JpaRepository<TexoraParticipant, Long> {
    List<TexoraParticipant> findByTexoraMeetingId(String texoraMeetingId);
    Optional<TexoraParticipant> findByTexoraMeetingIdAndIdentityAndLeftAtIsNull(String texoraMeetingId, String identity);
}