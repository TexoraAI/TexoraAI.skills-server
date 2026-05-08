package com.lms.user.repo;

import com.lms.user.model.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    List<Resume> findByUserIdOrderByUpdatedAtDesc(Long userId);

    Optional<Resume> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    @Query("SELECT r FROM Resume r WHERE r.userId = :userId AND r.title LIKE %:keyword%")
    List<Resume> searchByTitle(@Param("userId") Long userId, @Param("keyword") String keyword);

    void deleteByIdAndUserId(Long id, Long userId);
}