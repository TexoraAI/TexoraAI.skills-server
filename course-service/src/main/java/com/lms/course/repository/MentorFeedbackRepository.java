package com.lms.course.repository;

import com.lms.course.model.MentorFeedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MentorFeedbackRepository extends JpaRepository<MentorFeedback, Long> {

    long countByStatus(MentorFeedback.FeedbackStatus status);

    long countByIsFeaturedTrue();

    @Query("""
            SELECT m FROM MentorFeedback m
            WHERE (:search IS NULL OR :search = '' OR
                   LOWER(m.candidateName) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(m.designation) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(m.company) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(m.feedbackMessage) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:status IS NULL OR m.status = :status)
              AND (:rating IS NULL OR m.rating = :rating)
            """)
    Page<MentorFeedback> search(
            @Param("search") String search,
            @Param("status") MentorFeedback.FeedbackStatus status,
            @Param("rating") Integer rating,
            Pageable pageable
    );

    @Query("SELECT m FROM MentorFeedback m WHERE m.status = 'ACTIVE' ORDER BY m.isFeatured DESC, m.createdAt DESC")
    List<MentorFeedback> findActiveForLandingPage();
}