 package com.lms.live_session.repository;
 import com.lms.live_session.entity.AiUploadedResource;
 import org.springframework.data.jpa.repository.JpaRepository;
 import org.springframework.stereotype.Repository;
 import java.util.List;

 @Repository
 public interface AiUploadedResourceRepository extends JpaRepository<AiUploadedResource, Long> {
     List<AiUploadedResource> findByUploadedByOrderByCreatedAtDesc(String uploadedBy);
     List<AiUploadedResource> findBySessionIdOrderByCreatedAtDesc(Long sessionId);
 }