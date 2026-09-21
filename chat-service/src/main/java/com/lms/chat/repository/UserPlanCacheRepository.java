package com.lms.chat.repository;

import com.lms.chat.entity.UserPlanCache;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPlanCacheRepository extends JpaRepository<UserPlanCache, String> {
}