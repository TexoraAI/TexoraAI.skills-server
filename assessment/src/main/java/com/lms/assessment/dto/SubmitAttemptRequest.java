//package com.lms.assessment.dto;
//
//import java.util.List;
//
//public class SubmitAttemptRequest {
//
//    private Long quizId;
//    private Long userId;
//    private List<AnswerRequest> answers;
//
//    public SubmitAttemptRequest() {}
//
//    public Long getQuizId() {
//        return quizId;
//    }
//
//    public void setQuizId(Long quizId) {
//        this.quizId = quizId;
//    }
//
//    public Long getUserId() {
//        return userId;
//    }
//
//    public void setUserId(Long userId) {
//        this.userId = userId;
//    }
//
//    public List<AnswerRequest> getAnswers() {
//        return answers;
//    }
//
//    public void setAnswers(List<AnswerRequest> answers) {
//        this.answers = answers;
//    }
//}


package com.lms.assessment.dto;

import java.util.List;

public class SubmitAttemptRequest {

    private Long quizId;
    private List<AnswerRequest> answers;

    public SubmitAttemptRequest() {}

    public Long getQuizId() {
        return quizId;
    }
//fvfvfv
    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }

    public List<AnswerRequest> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerRequest> answers) {
        this.answers = answers;
    }
}
