package com.lms.course.repository;

import com.lms.course.model.ProgramWishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgramWishlistRepository extends JpaRepository<ProgramWishlist, Long> {

    boolean existsByProgramIdAndUserEmail(Long programId, String userEmail);

    List<ProgramWishlist> findAllByUserEmail(String userEmail);

    long countByProgramId(Long programId);

    void deleteByProgramIdAndUserEmail(Long programId, String userEmail);
}