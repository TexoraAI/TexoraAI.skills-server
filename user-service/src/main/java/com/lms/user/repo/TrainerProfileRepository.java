package com.lms.user.repo;

import com.lms.user.model.TrainerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TrainerProfileRepository extends JpaRepository<TrainerProfile, Long> {
    Optional<TrainerProfile> findByUser_Email(String email);
    void deleteByUserId(Long userId);
}