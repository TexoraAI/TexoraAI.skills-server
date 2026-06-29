package com.lms.course.repository;

import com.lms.course.model.FeaturedProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeaturedProgramRepository extends JpaRepository<FeaturedProgram, Long> {

    List<FeaturedProgram> findAllByStatusOrderByDisplayOrderAsc(String status);

    List<FeaturedProgram> findAllByCategoryIgnoreCaseAndStatus(String category, String status);

    Optional<FeaturedProgram> findBySlug(String slug);

    List<FeaturedProgram> findAllByOrderByDisplayOrderAsc();

    long countByStatus(String status);

    @Query("SELECT DISTINCT f.category FROM FeaturedProgram f")
    List<String> findDistinctCategories();
}