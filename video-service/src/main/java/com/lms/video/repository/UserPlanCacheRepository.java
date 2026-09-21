package com.lms.video.repository;

import com.lms.video.model.UserPlanCache;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPlanCacheRepository extends JpaRepository<UserPlanCache, String> {
}