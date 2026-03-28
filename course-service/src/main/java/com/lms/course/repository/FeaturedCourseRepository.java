package com.lms.course.repository;

import com.lms.course.model.FeaturedCourse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeaturedCourseRepository extends JpaRepository<FeaturedCourse, Long> {
}