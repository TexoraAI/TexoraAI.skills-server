 package com.lms.live_session.repository;
 import com.lms.live_session.entity.AiActivityLog;
 import org.springframework.data.jpa.repository.JpaRepository;
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.Pageable;
 import org.springframework.stereotype.Repository;

 @Repository
 public interface AiActivityLogRepository extends JpaRepository<AiActivityLog, Long> {
     Page<AiActivityLog> findByUserEmailOrderByCreatedAtDesc(String userEmail, Pageable pageable);
     Page<AiActivityLog> findAllByOrderByCreatedAtDesc(Pageable pageable); // admin use
 }