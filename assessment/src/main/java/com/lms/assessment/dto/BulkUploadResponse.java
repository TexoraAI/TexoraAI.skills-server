//package com.lms.assessment.dto;
//
//import java.util.List;
//import java.util.Map;
//
//public class BulkUploadResponse {
//
//    private String title;
//    private List<QuestionDTO> questions;
//
//    // ── Getters & Setters ──────────────────────────────────────────────────────
//
//    public String getTitle() { return title; }
//    public void setTitle(String title) { this.title = title; }
//
//    public List<QuestionDTO> getQuestions() { return questions; }
//    public void setQuestions(List<QuestionDTO> questions) { this.questions = questions; }
//
//    // ── Inner DTO ──────────────────────────────────────────────────────────────
//
//    public static class QuestionDTO {
//
//        private String text;
//        private Map<String, String> options; // keys: "A", "B", "C", "D"
//        private String correctOption;        // "A" | "B" | "C" | "D"
//
//        public String getText() { return text; }
//        public void setText(String text) { this.text = text; }
//
//        public Map<String, String> getOptions() { return options; }
//        public void setOptions(Map<String, String> options) { this.options = options; }
//
//        public String getCorrectOption() { return correctOption; }
//        public void setCorrectOption(String correctOption) { this.correctOption = correctOption; }
//    }
//}





package com.lms.assessment.dto;
import java.util.List;
import java.util.Map;

public class BulkUploadResponse {
    private String title;
    private String quizType;
    private List<QuestionDTO> questions;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getQuizType() { return quizType; }
    public void setQuizType(String quizType) { this.quizType = quizType; }

    public List<QuestionDTO> getQuestions() { return questions; }
    public void setQuestions(List<QuestionDTO> questions) { this.questions = questions; }

    public static class QuestionDTO {
        private String text;
        private Map<String, String> options;
        private String correctOption;
        private String answer;

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public Map<String, String> getOptions() { return options; }
        public void setOptions(Map<String, String> options) { this.options = options; }

        public String getCorrectOption() { return correctOption; }
        public void setCorrectOption(String correctOption) { this.correctOption = correctOption; }

        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }
    }
}