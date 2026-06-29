//package com.lms.user.repo;
//
//import com.lms.user.model.TrainerProfile;
//import org.springframework.data.jpa.repository.JpaRepository;
//import java.util.Optional;
//
//public interface TrainerProfileRepository extends JpaRepository<TrainerProfile, Long> {
//    Optional<TrainerProfile> findByUser_Email(String email);
//    void deleteByUserId(Long userId);
//}
package com.lms.user.repo;

import com.lms.user.model.TrainerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

public interface TrainerProfileRepository extends JpaRepository<TrainerProfile, Long> {
    Optional<TrainerProfile> findByUser_Email(String email);

    @Modifying
    @Transactional
    void deleteByUserId(Long userId);
}