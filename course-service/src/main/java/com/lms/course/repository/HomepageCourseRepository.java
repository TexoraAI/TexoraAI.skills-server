package com.lms.course.repository;

import com.lms.course.model.HomepageCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HomepageCourseRepository extends JpaRepository<HomepageCourse, Long> {

    List<HomepageCourse> findByActiveTrueAndFeaturedTrueOrderByIdAsc();

    Optional<HomepageCourse> findByIdAndActiveTrue(Long id);
}