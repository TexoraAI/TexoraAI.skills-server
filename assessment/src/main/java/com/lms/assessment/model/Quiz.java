//
//
//package com.lms.assessment.model;
//
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import jakarta.persistence.*;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Entity
//@Table(name = "quizzes")
//@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
//public class Quiz {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private String title;
//    private String courseId;
//
//    // ✅ NEW: Soft delete flag
//    @Column(nullable = false)
//    private boolean active = true;
//
//    @OneToMany(mappedBy = "quiz", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    @JsonIgnoreProperties({"quiz"})
//    private List<Question> questions = new ArrayList<>();
//    
//    @Column(name = "batch_id", nullable = false)
//    private Long batchId;
//
//    
//    
//    @Column(name = "trainer_email", nullable = false)
//    private String trainerEmail;
//
//    public String getTrainerEmail() {
//        return trainerEmail;
//    }
//
//    public void setTrainerEmail(String trainerEmail) {
//        this.trainerEmail = trainerEmail;
//    }
//    // =========================
//    // GETTERS & SETTERS
//    // =========================
//
//    public Long getBatchId()
//    {
//    	return batchId;
//    }
//    public void setBatchId(Long batchId)
//    {
//    	this.batchId=batchId;
//    }
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public String getTitle() {
//        return title;
//    }
//
//    public void setTitle(String title) {
//        this.title = title;
//    }
//
//    public String getCourseId() {
//        return courseId;
//    }
//
//    public void setCourseId(String courseId) {
//        this.courseId = courseId;
//    }
//
//    public List<Question> getQuestions() {
//        return questions;
//    }
//
//    public void setQuestions(List<Question> questions) {
//        this.questions = questions;
//    }
//
//    // ✅ NEW getters for active flag
//    public boolean isActive() {
//        return active;
//    }
//
//    public void setActive(boolean active) {
//        this.active = active;
//    }
//}
//
package com.lms.assessment.model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quizzes")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String courseId;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "quiz", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"quiz"})
    private List<Question> questions = new ArrayList<>();

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(name = "trainer_email", nullable = false)
    private String trainerEmail;

    // ✅ NEW FIELDS
    @Column(name = "quiz_type")
    private String quizType;

    @Column(name = "difficulty")
    private String difficulty;

    @Column(name = "category")
    private String category;

    @Column(name = "time_limit")
    private Integer timeLimit;

    @Column(name = "total_marks")
    private Integer totalMarks;

    // ── Getters & Setters ──────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getTrainerEmail() { return trainerEmail; }
    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }

    public String getQuizType() { return quizType; }
    public void setQuizType(String quizType) { this.quizType = quizType; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getTimeLimit() { return timeLimit; }
    public void setTimeLimit(Integer timeLimit) { this.timeLimit = timeLimit; }

    public Integer getTotalMarks() { return totalMarks; }
    public void setTotalMarks(Integer totalMarks) { this.totalMarks = totalMarks; }
}