//
//package com.lms.user.repo;
//
//import com.lms.user.model.StudentProfile;
//import org.springframework.data.jpa.repository.JpaRepository;
//import java.util.Optional;
//
//public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {
//    Optional<StudentProfile> findByUser_Email(String email); 
//    // underscore = traverse relationship
//    Optional<StudentProfile> findByUserId(Long userId);
//    
//    void deleteByUserId(Long userId);
//}
package com.lms.user.repo;

import com.lms.user.model.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {
    Optional<StudentProfile> findByUser_Email(String email);
    Optional<StudentProfile> findByUserId(Long userId);

    @Modifying
    @Transactional
    void deleteByUserId(Long userId);
}