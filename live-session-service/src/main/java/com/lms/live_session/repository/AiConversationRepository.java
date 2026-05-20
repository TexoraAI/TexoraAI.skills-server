 package com.lms.live_session.repository;
 import com.lms.live_session.entity.AiConversation;
 import org.springframework.data.jpa.repository.JpaRepository;
 import org.springframework.stereotype.Repository;
 import java.util.List;

 @Repository
 public interface AiConversationRepository extends JpaRepository<AiConversation, Long> {
     List<AiConversation> findByUserEmailOrderByUpdatedAtDesc(String userEmail);
     List<AiConversation> findByUserEmailAndStatusOrderByUpdatedAtDesc(String userEmail, String status);
     List<AiConversation> findByUserEmailAndSessionIdOrderByUpdatedAtDesc(String userEmail, Long sessionId);
 }