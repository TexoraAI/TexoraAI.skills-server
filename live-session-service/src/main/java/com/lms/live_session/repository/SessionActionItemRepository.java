 package com.lms.live_session.repository;
 import com.lms.live_session.entity.SessionActionItem;
 import org.springframework.data.jpa.repository.JpaRepository;
 import org.springframework.stereotype.Repository;
 import java.util.List;

 @Repository
 public interface SessionActionItemRepository extends JpaRepository<SessionActionItem, Long> {
     List<SessionActionItem> findBySessionIdOrderByCreatedAtDesc(Long sessionId);
     List<SessionActionItem> findBySessionIdAndStatus(Long sessionId, String status);
 }