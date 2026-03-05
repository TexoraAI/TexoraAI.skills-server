package com.lms.assessment.dto;

public class AnswerRequest {

    private Long questionId;
    private Long selectedOptionId;

    public AnswerRequest() {}

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }
//djndjhnd
    public Long getSelectedOptionId() {
        return selectedOptionId;
    }

    public void setSelectedOptionId(Long selectedOptionId) {
        this.selectedOptionId = selectedOptionId;
    }
}
