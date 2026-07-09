package com.lms.chat.repository;

import com.lms.chat.entity.ChatFeatureFlags;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatFeatureFlagsRepository extends JpaRepository<ChatFeatureFlags, String> {
    Optional<ChatFeatureFlags> findByScopeKey(String scopeKey);
}