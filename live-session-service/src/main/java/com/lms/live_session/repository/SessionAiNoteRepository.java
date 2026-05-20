 package com.lms.live_session.repository;
 import com.lms.live_session.entity.SessionAiNote;
 import org.springframework.data.jpa.repository.JpaRepository;
 import org.springframework.stereotype.Repository;
 import java.util.List;

 @Repository
 public interface SessionAiNoteRepository extends JpaRepository<SessionAiNote, Long> {
     List<SessionAiNote> findBySessionIdOrderByCreatedAtDesc(Long sessionId);
 }