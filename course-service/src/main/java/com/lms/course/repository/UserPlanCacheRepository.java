package com.lms.course.repository;

import com.lms.course.model.UserPlanCache;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPlanCacheRepository extends JpaRepository<UserPlanCache, String> {
}