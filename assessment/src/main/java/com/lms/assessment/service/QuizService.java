//
//
//package com.lms.assessment.service;
//
//import com.lms.assessment.dto.*;
//import com.lms.assessment.kafka.AssessmentEventProducer;
//import com.lms.assessment.model.*;
//import com.lms.assessment.repository.*;
//import jakarta.transaction.Transactional;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//public class QuizService {
//
//    private final QuizRepository               quizRepo;
//    private final QuestionRepository           questionRepo;
//    private final OptionRepository             optionRepo;
//    private final AttemptRepository            attemptRepo;
//    private final AnswerRepository             answerRepo;
//    private final AssessmentEventProducer      producer;
//    private final TrainerBatchMapRepository    trainerBatchMapRepository;
//    private final StudentBatchMapRepository    studentBatchMapRepository;
//    private final StudentTrainerMapRepository  studentTrainerMapRepository;
//    private final StudentQuizMapRepository     studentQuizMapRepository;
//
//    public QuizService(
//            QuizRepository              quizRepo,
//            QuestionRepository          questionRepo,
//            OptionRepository            optionRepo,
//            AttemptRepository           attemptRepo,
//            AnswerRepository            answerRepo,
//            AssessmentEventProducer     producer,
//            TrainerBatchMapRepository   trainerBatchMapRepository,
//            StudentBatchMapRepository   studentBatchMapRepository,
//            StudentTrainerMapRepository studentTrainerMapRepository,
//            StudentQuizMapRepository    studentQuizMapRepository) {
//
//        this.quizRepo                   = quizRepo;
//        this.questionRepo               = questionRepo;
//        this.optionRepo                 = optionRepo;
//        this.attemptRepo                = attemptRepo;
//        this.answerRepo                 = answerRepo;
//        this.producer                   = producer;
//        this.trainerBatchMapRepository  = trainerBatchMapRepository;
//        this.studentBatchMapRepository  = studentBatchMapRepository;
//        this.studentTrainerMapRepository = studentTrainerMapRepository;
//        this.studentQuizMapRepository   = studentQuizMapRepository;
//    }
//
//    // ================= CREATE QUIZ (simple) =================
//
//    @Transactional
//    public Quiz createQuiz(Quiz quiz, String trainerEmail) {
//
//        quiz.setTrainerEmail(trainerEmail);
//        quiz.setActive(true);
//
//        Quiz savedQuiz = quizRepo.save(quiz);
//
//        assignQuizToTrainerStudents(
//                savedQuiz.getId(),
//                trainerEmail,
//                savedQuiz.getBatchId()
//        );
//
//        // publish Kafka event
//        try {
//            producer.publishQuizCreated(
//                    savedQuiz.getId(),
//                    savedQuiz.getTitle(),
//                    savedQuiz.getBatchId(),
//                    savedQuiz.getTrainerEmail()
//            );
//        } catch (Exception e) {
//            System.out.println("Kafka unavailable, skipping QUIZ_CREATED event");
//        }
//
//        return savedQuiz;
//    }
//
//    
//    @Transactional
//    public Quiz createQuizWithQuestions(CreateQuizWithQuestionsRequest req,
//                                         String trainerEmail) {
//        Quiz quiz = new Quiz();
//        quiz.setTitle(req.getTitle());
//        quiz.setCourseId(req.getCourseId());
//        quiz.setBatchId(req.getBatchId());
//        quiz.setTrainerEmail(trainerEmail);
//        quiz.setActive(true);
//
//        // ✅ NEW FIELDS
//        quiz.setQuizType(req.getQuizType());
//        quiz.setDifficulty(req.getDifficulty());
//        quiz.setCategory(req.getCategory());
//        quiz.setTimeLimit(req.getTimeLimit());
//        quiz.setTotalMarks(req.getTotalMarks());
//
//        List<Question> questions = new ArrayList<>();
//
//        for (CreateQuizWithQuestionsRequest.QuestionRequest qReq : req.getQuestions()) {
//            Question question = new Question();
//            question.setText(qReq.getText());
//            question.setQuiz(quiz);
//
//            List<Option> options = new ArrayList<>();
//            for (CreateOptionRequest oReq : qReq.getOptions()) {
//                Option option = new Option();
//                option.setText(oReq.getText());
//                option.setCorrect(oReq.isCorrect());
//                option.setQuestion(question);
//                options.add(option);
//            }
//            question.setOptions(options);
//            questions.add(question);
//        }
//
//        quiz.setQuestions(questions);
//        Quiz savedQuiz = quizRepo.save(quiz);
//
//        assignQuizToTrainerStudents(
//                savedQuiz.getId(),
//                trainerEmail,
//                savedQuiz.getBatchId()
//        );
//
//        try {
//            producer.publishQuizCreated(
//                    savedQuiz.getId(),
//                    savedQuiz.getTitle(),
//                    savedQuiz.getBatchId(),
//                    savedQuiz.getTrainerEmail()
//            );
//        } catch (Exception e) {
//            System.out.println("Kafka unavailable, skipping QUIZ_CREATED event");
//        }
//
//        return savedQuiz;
//    }
//    
//    
//    // ================= GET QUIZ =================
//
//    public Quiz getQuiz(Long id) {
//        Quiz quiz = quizRepo.findQuizWithQuestions(id)
//            .orElseThrow(() -> new RuntimeException("Quiz not found"));
//        System.out.println("NEW METHOD CALLED");
//        // load options separately
//        quiz.getQuestions().forEach(q -> q.getOptions().size());
//
//        return quiz;
//    }
//    // ================= TRAINER — SOFT DELETE =================
//
//    @Transactional
//    public void deleteQuizByTrainer(Long quizId, String trainerEmail) {
//
//        Quiz quiz = quizRepo.findById(quizId)
//                .orElseThrow(() -> new RuntimeException("Quiz not found"));
//
//        if (!quiz.getTrainerEmail().equals(trainerEmail)) {
//            throw new RuntimeException("You are not allowed to delete this quiz");
//        }
//
//        quiz.setActive(false);
//        quizRepo.save(quiz);
//    }
//
//    // ================= SUBMIT =================
//
//    public QuizResultResponse submitAnswers(SubmitAttemptRequest req) {
//
//        Quiz quiz = quizRepo.findById(req.getQuizId())
//                .orElseThrow(() -> new RuntimeException("Quiz not found"));
//
//        int total   = quiz.getQuestions().size();
//        int correct = 0;
//
//        for (AnswerRequest ar : req.getAnswers()) {
//            Option opt = optionRepo.findById(ar.getSelectedOptionId())
//                    .orElseThrow(() -> new RuntimeException("Option not found"));
//            if (opt.isCorrect()) correct++;
//        }
//
//        double percentage = (correct * 100.0) / total;
//
//        QuizResultResponse res = new QuizResultResponse();
//        res.setTotalQuestions(total);
//        res.setCorrectAnswers(correct);
//        res.setPercentage(percentage);
//
//        return res;
//    }
//
//    // ================= GET BY TRAINER =================
//
//    public List<Quiz> getTrainerQuizzes(String email) {
//        return quizRepo.findByTrainerEmailAndActiveTrue(email);
//    }
//
//    // ================= GET BY STUDENT =================
//
//
//    
//    public List<Quiz> getStudentQuizzes(String email) {
//
//        List<StudentBatchMap> list =
//                studentBatchMapRepository.findAllByStudentEmail(email);
//
//        if (list.isEmpty()) {
//            System.out.println("❌ No batch found for email: " + email);
//            return List.of(); // no crash
//        }
//
//        Long batchId = list.get(0).getBatchId();
//
//        return quizRepo.findByBatchIdAndActiveTrue(batchId);
//    }
//
//    public Quiz getQuizForStudent(Long quizId, String studentEmail) {
//
//        boolean allowed = studentQuizMapRepository
//                .existsByQuizIdAndStudentEmail(quizId, studentEmail);
//
//        if (!allowed)
//            throw new RuntimeException("You are not allowed to access this quiz");
//
//        // ✅ USE THE SAME METHOD THAT FORCE-LOADS QUESTIONS + OPTIONS
//        Quiz quiz = quizRepo.findQuizWithQuestions(quizId)
//                .orElseThrow(() -> new RuntimeException("Quiz not found"));
//
//        if (!quiz.isActive())
//            throw new RuntimeException("Quiz not found");
//
//        // ✅ Force-load lazy options for each question
//        quiz.getQuestions().forEach(q -> q.getOptions().size());
//
//        return quiz;
//    }
//    // ================= ASSIGN TO STUDENTS (private) =================
//
//    private void assignQuizToTrainerStudents(Long quizId,
//                                              String trainerEmail,
//                                              Long batchId) {
//
//        List<String> students = studentTrainerMapRepository
//                .findActiveStudentsByTrainerAndBatch(trainerEmail, batchId);
//
//        for (String student : students) {
//            studentQuizMapRepository.save(new StudentQuizMap(quizId, student));
//        }
//    }
//}
package com.lms.assessment.service;

import com.lms.assessment.dto.*;
import com.lms.assessment.kafka.AssessmentEventProducer;
import com.lms.assessment.model.*;
import com.lms.assessment.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;

@Service
public class QuizService {

    private final QuizRepository               quizRepo;
    private final QuestionRepository           questionRepo;
    private final OptionRepository             optionRepo;
    private final AttemptRepository            attemptRepo;
    private final AnswerRepository             answerRepo;
    private final AssessmentEventProducer      producer;
    private final TrainerBatchMapRepository    trainerBatchMapRepository;
    private final StudentBatchMapRepository    studentBatchMapRepository;
    private final StudentTrainerMapRepository  studentTrainerMapRepository;
    private final StudentQuizMapRepository     studentQuizMapRepository;

    public QuizService(
            QuizRepository              quizRepo,
            QuestionRepository          questionRepo,
            OptionRepository            optionRepo,
            AttemptRepository           attemptRepo,
            AnswerRepository            answerRepo,
            AssessmentEventProducer     producer,
            TrainerBatchMapRepository   trainerBatchMapRepository,
            StudentBatchMapRepository   studentBatchMapRepository,
            StudentTrainerMapRepository studentTrainerMapRepository,
            StudentQuizMapRepository    studentQuizMapRepository) {

        this.quizRepo                   = quizRepo;
        this.questionRepo               = questionRepo;
        this.optionRepo                 = optionRepo;
        this.attemptRepo                = attemptRepo;
        this.answerRepo                 = answerRepo;
        this.producer                   = producer;
        this.trainerBatchMapRepository  = trainerBatchMapRepository;
        this.studentBatchMapRepository  = studentBatchMapRepository;
        this.studentTrainerMapRepository = studentTrainerMapRepository;
        this.studentQuizMapRepository   = studentQuizMapRepository;
    }

    // ================= CREATE QUIZ (simple) =================

    @Transactional
    public Quiz createQuiz(Quiz quiz, String trainerEmail, String organizationId) {

        quiz.setTrainerEmail(trainerEmail);
        quiz.setActive(true);
        quiz.setOrganizationId(organizationId); // null for standalone trainers — expected

        Quiz savedQuiz = quizRepo.save(quiz);

        assignQuizToTrainerStudents(
                savedQuiz.getId(),
                trainerEmail,
                savedQuiz.getBatchId()
        );

        try {
            producer.publishQuizCreated(
                    savedQuiz.getId(),
                    savedQuiz.getTitle(),
                    savedQuiz.getBatchId(),
                    savedQuiz.getTrainerEmail()
            );
        } catch (Exception e) {
            System.out.println("Kafka unavailable, skipping QUIZ_CREATED event");
        }

        return savedQuiz;
    }

    @Transactional
    public Quiz createQuizWithQuestions(CreateQuizWithQuestionsRequest req,
                                         String trainerEmail,
                                         String organizationId) {
        Quiz quiz = new Quiz();
        quiz.setTitle(req.getTitle());
        quiz.setCourseId(req.getCourseId());
        quiz.setBatchId(req.getBatchId());
        quiz.setTrainerEmail(trainerEmail);
        quiz.setActive(true);
        quiz.setOrganizationId(organizationId); // null for standalone trainers — expected

        quiz.setQuizType(req.getQuizType());
        quiz.setDifficulty(req.getDifficulty());
        quiz.setCategory(req.getCategory());
        quiz.setTimeLimit(req.getTimeLimit());
        quiz.setTotalMarks(req.getTotalMarks());

        List<Question> questions = new ArrayList<>();

        for (CreateQuizWithQuestionsRequest.QuestionRequest qReq : req.getQuestions()) {
            Question question = new Question();
            question.setText(qReq.getText());
            question.setQuiz(quiz);

            List<Option> options = new ArrayList<>();
            for (CreateOptionRequest oReq : qReq.getOptions()) {
                Option option = new Option();
                option.setText(oReq.getText());
                option.setCorrect(oReq.isCorrect());
                option.setQuestion(question);
                options.add(option);
            }
            question.setOptions(options);
            questions.add(question);
        }

        quiz.setQuestions(questions);
        Quiz savedQuiz = quizRepo.save(quiz);

        assignQuizToTrainerStudents(
                savedQuiz.getId(),
                trainerEmail,
                savedQuiz.getBatchId()
        );

        try {
            producer.publishQuizCreated(
                    savedQuiz.getId(),
                    savedQuiz.getTitle(),
                    savedQuiz.getBatchId(),
                    savedQuiz.getTrainerEmail()
            );
        } catch (Exception e) {
            System.out.println("Kafka unavailable, skipping QUIZ_CREATED event");
        }

        return savedQuiz;
    }

    // ================= GET QUIZ =================

    public Quiz getQuiz(Long id, String organizationId) {
        Quiz quiz = quizRepo.findQuizWithQuestions(id)
            .orElseThrow(() -> new RuntimeException("Quiz not found"));

        assertSameOrg(quiz.getOrganizationId(), organizationId);

        quiz.getQuestions().forEach(q -> q.getOptions().size());

        return quiz;
    }

    // ================= TRAINER — SOFT DELETE =================

    @Transactional
    public void deleteQuizByTrainer(Long quizId, String trainerEmail, String organizationId) {

        Quiz quiz = quizRepo.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        assertSameOrg(quiz.getOrganizationId(), organizationId);

        if (!quiz.getTrainerEmail().equals(trainerEmail)) {
            throw new RuntimeException("You are not allowed to delete this quiz");
        }

        quiz.setActive(false);
        quizRepo.save(quiz);
    }

    // ================= SUBMIT =================

    public QuizResultResponse submitAnswers(SubmitAttemptRequest req, String organizationId) {

        Quiz quiz = quizRepo.findById(req.getQuizId())
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        assertSameOrg(quiz.getOrganizationId(), organizationId);

        int total   = quiz.getQuestions().size();
        int correct = 0;

        for (AnswerRequest ar : req.getAnswers()) {
            Option opt = optionRepo.findById(ar.getSelectedOptionId())
                    .orElseThrow(() -> new RuntimeException("Option not found"));
            if (opt.isCorrect()) correct++;
        }

        double percentage = (correct * 100.0) / total;

        QuizResultResponse res = new QuizResultResponse();
        res.setTotalQuestions(total);
        res.setCorrectAnswers(correct);
        res.setPercentage(percentage);

        return res;
    }

    // ================= GET BY TRAINER =================

    public List<Quiz> getTrainerQuizzes(String email, String organizationId) {
        if (organizationId == null) {
            return quizRepo.findByTrainerEmailAndActiveTrue(email);
        }
        return quizRepo.findByOrganizationIdAndTrainerEmailAndActiveTrue(organizationId, email);
    }

    // ================= GET BY STUDENT =================

    public List<Quiz> getStudentQuizzes(String email, String organizationId) {

        List<StudentBatchMap> list =
                studentBatchMapRepository.findAllByStudentEmail(email);

        if (list.isEmpty()) {
            System.out.println("❌ No batch found for email: " + email);
            return List.of();
        }

        Long batchId = list.get(0).getBatchId();

        List<Quiz> quizzes = quizRepo.findByBatchIdAndActiveTrue(batchId);

        if (organizationId != null) {
            quizzes.removeIf(q -> !organizationId.equals(q.getOrganizationId()));
        }

        return quizzes;
    }

    public Quiz getQuizForStudent(Long quizId, String studentEmail, String organizationId) {

        boolean allowed = studentQuizMapRepository
                .existsByQuizIdAndStudentEmail(quizId, studentEmail);

        if (!allowed)
            throw new RuntimeException("You are not allowed to access this quiz");

        Quiz quiz = quizRepo.findQuizWithQuestions(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        assertSameOrg(quiz.getOrganizationId(), organizationId);

        if (!quiz.isActive())
            throw new RuntimeException("Quiz not found");

        quiz.getQuestions().forEach(q -> q.getOptions().size());

        return quiz;
    }

    // ================= ASSIGN TO STUDENTS (private) =================

    private void assignQuizToTrainerStudents(Long quizId,
                                              String trainerEmail,
                                              Long batchId) {

        List<String> students = studentTrainerMapRepository
                .findActiveStudentsByTrainerAndBatch(trainerEmail, batchId);

        for (String student : students) {
            studentQuizMapRepository.save(new StudentQuizMap(quizId, student));
        }
    }

    // ================= ORG GUARD (private) =================

    // organizationId == null on the caller means standalone — no restriction.
    // organizationId != null means the resource must belong to the same org.
    private void assertSameOrg(String resourceOrgId, String callerOrgId) {
        if (callerOrgId == null) return;
        if (!callerOrgId.equals(resourceOrgId)) {
            throw new RuntimeException("Access denied: quiz belongs to a different organization");
        }
    }
    
    @Transactional
    public List<QuizAdminReportResponse> getAdminReport(String organizationId) {
        List<Quiz> quizzes = quizRepo.findByOrganizationId(organizationId);
        return quizzes.stream()
                .sorted(Comparator.comparing(Quiz::getId).reversed())
                .map(this::toAdminReport)
                .toList();
    }

    @Transactional
    public List<QuizAdminReportResponse> getSuperAdminReport() {
        List<Quiz> quizzes = quizRepo.findByOrganizationIdIsNull();
        return quizzes.stream()
                .sorted(Comparator.comparing(Quiz::getId).reversed())
                .map(this::toAdminReport)
                .toList();
    }

    private QuizAdminReportResponse toAdminReport(Quiz quiz) {
        int questionCount = quiz.getQuestions() != null ? quiz.getQuestions().size() : 0;
        long attemptCount = attemptRepo.countByQuiz_Id(quiz.getId());

        return new QuizAdminReportResponse(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getTrainerEmail(),
                quiz.getBatchId(),
                quiz.getQuizType(),
                quiz.getDifficulty(),
                quiz.getTotalMarks(),
                questionCount,
                attemptCount,
                quiz.getOrganizationId() != null ? quiz.getOrganizationId() : "Standalone"
        );
    }
}