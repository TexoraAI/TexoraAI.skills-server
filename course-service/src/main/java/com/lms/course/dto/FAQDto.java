package com.lms.course.dto;

public class FAQDto {

    private Long id;
    private String question;
    private String answer;
    private Integer orderIndex;

    public FAQDto() {
    }

    public FAQDto(Long id, String question, String answer, Integer orderIndex) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.orderIndex = orderIndex;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }
}