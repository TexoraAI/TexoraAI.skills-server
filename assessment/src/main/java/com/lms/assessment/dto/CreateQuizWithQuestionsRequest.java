//
//package com.lms.assessment.dto;
//
//import java.util.List;
//
//public class CreateQuizWithQuestionsRequest {
//
//    private String title;
//    private String courseId;
//    private List<QuestionRequest> questions;
//
//    public static class QuestionRequest {
//        private String text;
//        private List<CreateOptionRequest> options;
//
//        public String getText() {
//            return text;
//        }
//
//        public void setText(String text) {
//            this.text = text;
//        }
//
//        public List<CreateOptionRequest> getOptions() {
//            return options;
//        }
//
//        public void setOptions(List<CreateOptionRequest> options) {
//            this.options = options;
//        }
//    }
//
//    public String getTitle() { return title; }
//    public void setTitle(String title) { this.title = title; }
//
//    public String getCourseId() { return courseId; }
//    public void setCourseId(String courseId) { this.courseId = courseId; }
//
//    public List<QuestionRequest> getQuestions() { return questions; }
//    public void setQuestions(List<QuestionRequest> questions) { this.questions = questions; }
//}
//
package com.lms.assessment.dto;

import java.util.List;

public class CreateQuizWithQuestionsRequest {

    private String title;
    private String courseId;

    // ⭐ ADD THESE TWO FIELDS
    private String trainerEmail;
    private Long batchId;

    private List<QuestionRequest> questions;

    // =============================
    // INNER CLASS
    // =============================
    public static class QuestionRequest {
        private String text;
        private List<CreateOptionRequest> options;

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public List<CreateOptionRequest> getOptions() { return options; }
        public void setOptions(List<CreateOptionRequest> options) { this.options = options; }
    }

    // =============================
    // GETTERS / SETTERS
    // =============================
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public List<QuestionRequest> getQuestions() { return questions; }
    public void setQuestions(List<QuestionRequest> questions) { this.questions = questions; }

    // ⭐ ADD THESE METHODS
    public String getTrainerEmail() { return trainerEmail; }
    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
}