package com.lms.progress.repository;

import com.lms.progress.model.UserPlanCache;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPlanCacheRepository extends JpaRepository<UserPlanCache, String> {
}