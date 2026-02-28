package com.lms.course.repository;

import com.lms.course.model.ContentItem;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContentRepository extends JpaRepository<ContentItem, Long> {
	List<ContentItem> findByCourseId(Long courseId);
    List<ContentItem> findByCourseIdAndOwnerEmail(Long courseId, String ownerEmail);
    long countByCourseId(Long courseId);

}

