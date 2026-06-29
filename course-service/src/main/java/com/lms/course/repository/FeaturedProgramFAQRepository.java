package com.lms.course.repository;

import com.lms.course.model.FeaturedProgramFAQ;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeaturedProgramFAQRepository extends JpaRepository<FeaturedProgramFAQ, Long> {

    List<FeaturedProgramFAQ> findByProgramIdOrderByOrderIndexAsc(Long programId);

    void deleteByProgramId(Long programId);
}