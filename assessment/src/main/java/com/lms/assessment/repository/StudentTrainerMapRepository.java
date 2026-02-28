//package com.lms.assessment.repository;
//
//import com.lms.assessment.model.StudentTrainerMap;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//
//import java.util.List;
//
//public interface StudentTrainerMapRepository extends JpaRepository<StudentTrainerMap, Long> {
//
//
//	@Query("""
//			SELECT stm.studentEmail
//			FROM StudentTrainerMap stm
//			WHERE stm.trainerEmail = :trainerEmail
//			AND stm.batchId = :batchId
//			AND stm.active = true
//			""")
//			List<String> findActiveStudentsByTrainerAndBatch(String trainerEmail, Long batchId);
//}
package com.lms.assessment.repository;

import com.lms.assessment.model.StudentTrainerMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentTrainerMapRepository
        extends JpaRepository<StudentTrainerMap, Long> {

    @Query("""
        SELECT stm.studentEmail
        FROM StudentTrainerMap stm
        WHERE stm.trainerEmail = :trainerEmail
        AND stm.batchId = :batchId
        AND stm.active = true
    """)
    List<String> findActiveStudentsByTrainerAndBatch(
            @Param("trainerEmail") String trainerEmail,
            @Param("batchId") Long batchId
    );
}