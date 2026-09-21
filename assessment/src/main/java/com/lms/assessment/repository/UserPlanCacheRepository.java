package com.lms.assessment.repository;

import com.lms.assessment.model.UserPlanCache;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPlanCacheRepository extends JpaRepository<UserPlanCache, String> {
}