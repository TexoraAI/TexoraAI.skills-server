//package com.lms.student.repo;
//
//import com.lms.student.model.Student;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.Optional;
//
//public interface StudentRepository extends JpaRepository<Student, Long> {
//
//    Optional<Student> findByUserId(Long userId);
//
//    boolean existsByUserId(Long userId);
//}

package com.lms.student.repo;

import com.lms.student.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByUserId(Long userId); // authUserId
    
    Optional<Student> findByEmail(String email);

    boolean existsByUserId(Long userId); // authUserId
}
