package com.lms.progress.dto;

public class SkillUpsertRequest {

    private String studentEmail;
    private Long   batchId;
    private String trainerEmail;
    private String skillName;
    private Double quizScore;          // nullable — only update if provided
    private Double assignmentScore;    // nullable
    private Double videoScore;         // nullable

    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getTrainerEmail() { return trainerEmail; }
    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public Double getQuizScore() { return quizScore; }
    public void setQuizScore(Double quizScore) { this.quizScore = quizScore; }

    public Double getAssignmentScore() { return assignmentScore; }
    public void setAssignmentScore(Double assignmentScore) { this.assignmentScore = assignmentScore; }

    public Double getVideoScore() { return videoScore; }
    public void setVideoScore(Double videoScore) { this.videoScore = videoScore; }
}