//package com.lms.student.repo;
//
//import com.lms.student.model.Trainer;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//public interface TrainerRepository extends JpaRepository<Trainer, Long> {
//
//    boolean existsByEmail(String email);
//}


package com.lms.student.repo;

import com.lms.student.model.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    // 🔑 identity-based checks
    boolean existsByUserId(Long userId);

    Optional<Trainer> findByUserId(Long userId);
    
    Optional<Trainer> findByEmail(String email);

}

