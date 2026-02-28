


package com.lms.assessment.service;

import com.lms.assessment.dto.*;
import com.lms.assessment.kafka.AssessmentEventProducer;
import com.lms.assessment.model.*;
import com.lms.assessment.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuizService {

    private final QuizRepository quizRepo;
    private final QuestionRepository questionRepo;
    private final OptionRepository optionRepo;
    private final AttemptRepository attemptRepo;
    private final AnswerRepository answerRepo;
    private final AssessmentEventProducer producer;
    private final TrainerBatchMapRepository trainerBatchMapRepository;
    private final StudentBatchMapRepository studentBatchMapRepository;
    private StudentTrainerMapRepository studentTrainerMapRepository;
    private StudentQuizMapRepository studentQuizMapRepository;
    public QuizService(
            QuizRepository quizRepo,
            QuestionRepository questionRepo,
            OptionRepository optionRepo,
            AttemptRepository attemptRepo,
            AnswerRepository answerRepo,
            AssessmentEventProducer producer,
            TrainerBatchMapRepository trainerBatchMapRepository,
            StudentBatchMapRepository studentBatchMapRepository,
            StudentTrainerMapRepository studentTrainerMapRepository,
            StudentQuizMapRepository studentQuizMapRepository
    ) {
        this.quizRepo = quizRepo;
        this.questionRepo = questionRepo;
        this.optionRepo = optionRepo;
        this.attemptRepo = attemptRepo;
        this.answerRepo = answerRepo;
        this.producer = producer;
        this.trainerBatchMapRepository = trainerBatchMapRepository;
        this.studentBatchMapRepository = studentBatchMapRepository;
        this.studentTrainerMapRepository=studentTrainerMapRepository;
        this.studentQuizMapRepository=studentQuizMapRepository;
    }

    // =========================
    // CREATE QUIZ
    
    
    @Transactional
    public Quiz createQuiz(Quiz quiz, String trainerEmail) {

        quiz.setTrainerEmail(trainerEmail);
        quiz.setActive(true);

        // 1️⃣ Save quiz
        Quiz savedQuiz = quizRepo.save(quiz);

        // 2️⃣ Assign to trainer students
        assignQuizToTrainerStudents(
                savedQuiz.getId(),
                trainerEmail,
                savedQuiz.getBatchId()
        );

        return savedQuiz;
    }
    // CREATE QUIZ WITH QUESTIONS
    // =========================
//    @CacheEvict(value = {"allQuizzes"}, allEntries = true)
//    public Quiz createQuizWithQuestions(CreateQuizWithQuestionsRequest req) {
//
//        Quiz quiz = new Quiz();
//        quiz.setTitle(req.getTitle());
//        quiz.setCourseId(req.getCourseId());
//
//        List<Question> questions = new ArrayList<>();
//
//        for (CreateQuizWithQuestionsRequest.QuestionRequest qReq : req.getQuestions()) {
//
//            Question question = new Question();
//            question.setText(qReq.getText());
//            question.setQuiz(quiz);
//
//            List<Option> options = new ArrayList<>();
//
//            for (CreateOptionRequest oReq : qReq.getOptions()) {
//                Option option = new Option();
//                option.setText(oReq.getText());
//                option.setCorrect(oReq.isCorrect());
//                option.setQuestion(question);
//                options.add(option);
//            }
//
//            question.setOptions(options);
//            questions.add(question);
//        }
//
//        quiz.setQuestions(questions);
//        return quizRepo.save(quiz);
//    }
    @Transactional
    public Quiz createQuizWithQuestions(CreateQuizWithQuestionsRequest req, String trainerEmail) {

        Quiz quiz = new Quiz();
        quiz.setTitle(req.getTitle());
        quiz.setCourseId(req.getCourseId());
        quiz.setBatchId(req.getBatchId());
        quiz.setTrainerEmail(trainerEmail);
        quiz.setActive(true);

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

        // 🔥 SAVE QUIZ FIRST
        Quiz savedQuiz = quizRepo.save(quiz);

        // 🔥 MOST IMPORTANT LINE — ASSIGN TO STUDENTS
        assignQuizToTrainerStudents(
                savedQuiz.getId(),
                trainerEmail,
                savedQuiz.getBatchId()
        );

        return savedQuiz;
    }
    // =========================
    // STUDENT — GET QUIZ
    // =========================
    public Quiz getQuiz(Long id) {
        return quizRepo.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
    }

    // =========================
    // STUDENT — LIST QUIZZES
    // =========================
    
    // =========================
    // TRAINER — SOFT DELETE
    // =========================
    @Transactional
    public void deleteQuizByTrainer(Long quizId, String trainerEmail) {

        Quiz quiz = quizRepo.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        // 🔐 Security check
        if (!quiz.getTrainerEmail().equals(trainerEmail)) {
            throw new RuntimeException("You are not allowed to delete this quiz");
        }

        // Soft delete
        quiz.setActive(false);
        quizRepo.save(quiz);
    }

    // =========================
    // SUBMIT — MUST SEE DELETED QUIZ
    // =========================
    public QuizResultResponse submitAnswers(SubmitAttemptRequest req) {

        Quiz quiz = quizRepo.findById(req.getQuizId())
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        int total = quiz.getQuestions().size();
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
    public List<Quiz> getTrainerQuizzes(String email) {
        return quizRepo.findByTrainerEmailAndActiveTrue(email);
    }
//    public List<Quiz> getStudentQuizzes(String studentEmail) {
//        return quizRepo.findAssignedQuizzes(studentEmail);
//    }
//    public List<Quiz> getStudentQuizzes(String email) {
//
//        return quizRepo.findQuizzesAssignedToStudent(email);
//    }
    public List<Quiz> getStudentQuizzes(String email) {
    	Long batchId = studentBatchMapRepository
    	        .findBatchIdByStudentEmail(email)
    	        .orElseThrow(() -> new RuntimeException("Student batch not found"));
    	return quizRepo.findByBatchIdAndActiveTrue(batchId);
    }
    
    public Quiz getQuizForStudent(Long quizId, String studentEmail) {

        boolean allowed = studentQuizMapRepository
                .existsByQuizIdAndStudentEmail(quizId, studentEmail);

        if(!allowed)
            throw new RuntimeException("You are not allowed to access this quiz");

        return quizRepo.findByIdAndActiveTrue(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
    }
    
    private void assignQuizToTrainerStudents(Long quizId, String trainerEmail, Long batchId) {

        List<String> students = studentTrainerMapRepository
                .findActiveStudentsByTrainerAndBatch(trainerEmail, batchId);

        for (String student : students) {
            StudentQuizMap map = new StudentQuizMap(quizId, student);
            studentQuizMapRepository.save(map);
        }
    }
}

