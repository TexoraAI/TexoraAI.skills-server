package com.lms.assessment.kafka;

import com.lms.assessment.model.Assignment;
import com.lms.assessment.model.Quiz;
import com.lms.assessment.repository.AnswerRepository;
import com.lms.assessment.repository.AssignmentAttachmentRepository;
import com.lms.assessment.repository.AssignmentRepository;
import com.lms.assessment.repository.AssignmentSubmissionRepository;
import com.lms.assessment.repository.AttemptRepository;
import com.lms.assessment.repository.OptionRepository;
import com.lms.assessment.repository.QuestionRepository;
import com.lms.assessment.repository.QuizRepository;
import com.lms.assessment.repository.StudentBatchMapRepository;
import com.lms.assessment.repository.StudentQuizMapRepository;
import com.lms.assessment.repository.TrainerBatchMapRepository;

import jakarta.transaction.Transactional;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service

public class BatchLifecycleConsumer {

    private final AssignmentRepository assignmentRepo;
    private final QuizRepository quizRepo;
    private final StudentBatchMapRepository studentRepo;
    private final TrainerBatchMapRepository trainerRepo;
    private final AttemptRepository attemptRepository;
    private StudentQuizMapRepository studentQuizMapRepository;
    private final AssignmentSubmissionRepository   assignmentSubmissionRepository;
    private final AssignmentAttachmentRepository assignmentAttachmentRepository;
    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    public BatchLifecycleConsumer(AssignmentRepository assignmentRepo,
                                  QuizRepository quizRepo,
                                  StudentBatchMapRepository studentRepo,
                                  TrainerBatchMapRepository trainerRepo,AttemptRepository attemptRepository,StudentQuizMapRepository studentQuizMapRepository,AssignmentSubmissionRepository   assignmentSubmissionRepository,AssignmentAttachmentRepository assignmentAttachmentRepository,AnswerRepository answerRepository,QuestionRepository questionRepository,OptionRepository optionRepository) {
    	
    	
        this.assignmentRepo = assignmentRepo;
        this.quizRepo = quizRepo;
        this.studentRepo = studentRepo;
        this.trainerRepo = trainerRepo;
        this.attemptRepository=attemptRepository;
        this.studentQuizMapRepository=studentQuizMapRepository;
        this.assignmentSubmissionRepository=assignmentSubmissionRepository;
        this.assignmentAttachmentRepository=assignmentAttachmentRepository;
        this.answerRepository=answerRepository;
        this.questionRepository=questionRepository;
        this.optionRepository=optionRepository;
    }

    @KafkaListener(topics = "batch-lifecycle", groupId = "assessment-service-group")
    @Transactional
    public void consume(Map<String, Object> event) {

        String type = (String) event.get("type");

        if ("BATCH_DELETED".equals(type)) {

            Long batchId = ((Number) event.get("batchId")).longValue();

            // 1️⃣ Get all quizzes of this batch
            List<Quiz> quizzes = quizRepo.findByBatchId(batchId);

            for (Quiz quiz : quizzes) {

                // delete attempts of this quiz
                attemptRepository.deleteByQuizId(quiz.getId());

                // delete student quiz mapping if exists
                studentQuizMapRepository.deleteByQuizId(quiz.getId());
            }

            // 2️⃣ Delete quizzes (auto deletes questions + options)
            quizRepo.deleteByBatchId(batchId);

            // 3️⃣ Delete assignment submissions first
            List<Assignment> assignments = assignmentRepo.findByBatchId(batchId);

            for (Assignment assignment : assignments) {
                assignmentSubmissionRepository.deleteByAssignmentId(assignment.getId());
                assignmentAttachmentRepository.deleteByAssignmentId(assignment.getId());
            }

            // 4️⃣ Delete assignments
            assignmentRepo.deleteByBatchId(batchId);

            // 5️⃣ Delete mapping tables
            studentRepo.deleteByBatchId(batchId);
            trainerRepo.deleteByBatchId(batchId);

            System.out.println("🔥 COMPLETE CLEANUP DONE FOR BATCH -> " + batchId);
        }

        if ("BRANCH_DELETED".equals(type)) {

            // 1️⃣ Delete deepest child tables first
            answerRepository.deleteAll();
            attemptRepository.deleteAll();
            studentQuizMapRepository.deleteAll();
            assignmentSubmissionRepository.deleteAll();
            assignmentAttachmentRepository.deleteAll();

            // 2️⃣ Delete quiz structure
            optionRepository.deleteAll();
            questionRepository.deleteAll();
            quizRepo.deleteAll();

            // 3️⃣ Delete assignments
            assignmentRepo.deleteAll();

            // 4️⃣ Delete mappings
            studentRepo.deleteAll();
            trainerRepo.deleteAll();

            System.out.println("🔥 FULL ASSESSMENT RESET COMPLETED");
        }
    }
}
