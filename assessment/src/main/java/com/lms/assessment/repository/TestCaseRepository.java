package com.lms.assessment.repository;

import com.lms.assessment.model.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Long> {

    // Used by JudgeService — runs all test cases for a problem
    List<TestCase> findByProblemIdOrderById(Long problemId);

    // Used by CodingProblemService.getTestCases()
    // (same method — already covered above)

    // Count test cases per problem (for validation)
    long countByProblemId(Long problemId);

    // Used to get only visible test cases for students
    List<TestCase> findByProblemIdAndIsHiddenFalseOrderById(Long problemId);

    // Delete all test cases when problem is hard-deleted
    void deleteAllByProblemId(Long problemId);
}