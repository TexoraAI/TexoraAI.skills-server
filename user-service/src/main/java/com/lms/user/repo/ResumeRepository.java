//package com.lms.user.repo;
//
//import com.lms.user.model.Resume;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface ResumeRepository extends JpaRepository<Resume, Long> {
//
//    List<Resume> findByUserIdOrderByUpdatedAtDesc(Long userId);
//
//    Optional<Resume> findByIdAndUserId(Long id, Long userId);
//
//    long countByUserId(Long userId);
//
//    @Query("SELECT r FROM Resume r WHERE r.userId = :userId AND r.title LIKE %:keyword%")
//    List<Resume> searchByTitle(@Param("userId") Long userId, @Param("keyword") String keyword);
//
//    void deleteByIdAndUserId(Long id, Long userId);
//}


package com.lms.user.repo;

import com.lms.user.dto.ResumeListItemDTO;
import com.lms.user.model.Resume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// WHY: Data access layer for student resume CRUD and AI-assisted resume generation persistence
@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    // WHY: Primary query for resume list page — filtered by user and sorted by newest
    // OPTIMIZATION: Added Pageable overload for pagination
    

    // WHY: Non-paginated kept for internal duplicate/score operations
    List<Resume> findByUserIdOrderByUpdatedAtDesc(Long userId);

    // WHY: Ownership check — ensures student can only access their own resume
    Optional<Resume> findByIdAndUserId(Long id, Long userId);

    // WHY: Used to enforce max resume limit per user (business rule)
    long countByUserId(Long userId);

    // WHY: Resume search within user's own resumes by title keyword
    // NOTE: LIKE %keyword% cannot use index — acceptable at current scale; consider full-text at 100K
    @Query("SELECT r FROM Resume r WHERE r.userId = :userId AND r.title LIKE %:keyword%")
    List<Resume> searchByTitle(@Param("userId") Long userId, @Param("keyword") String keyword);

    void deleteByIdAndUserId(Long id, Long userId);
    
 // WHY: Resume list view needs only card-level fields — avoids loading all child collections
    @Query("SELECT new com.lms.user.dto.ResumeListItemDTO(r.id, r.title, r.templateName, r.resumeScore, r.isAtsFriendly, r.updatedAt, r.createdAt, r.jobTitle) " +
           "FROM Resume r WHERE r.userId = :userId ORDER BY r.updatedAt DESC")
    Page<ResumeListItemDTO> findResumeListByUserId(@Param("userId") Long userId, Pageable pageable);
    
    
 // Paginated — for list endpoint
    Page<Resume> findByUserIdOrderByUpdatedAtDesc(Long userId, Pageable pageable);

    // Non-paginated — still used by duplicate/score operations internally
 
}