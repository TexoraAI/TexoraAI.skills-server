package com.lms.progress.dto;

import java.util.List;

public class TrainerBatchSkillResponse {

    private Long   batchId;
    private String trainerEmail;
    private int    totalStudents;
    private int    strongStudents;      // all skills >= 70
    private int    weakStudents;        // any skill < 50
    private double batchAvgScore;
    private List<SkillAvgEntry>          skillAverages;  // per-skill avg → radar + bar chart
    private List<TrainerStudentSkillRow> students;       // individual student rows

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getTrainerEmail() { return trainerEmail; }
    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }

    public int getTotalStudents() { return totalStudents; }
    public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }

    public int getStrongStudents() { return strongStudents; }
    public void setStrongStudents(int strongStudents) { this.strongStudents = strongStudents; }

    public int getWeakStudents() { return weakStudents; }
    public void setWeakStudents(int weakStudents) { this.weakStudents = weakStudents; }

    public double getBatchAvgScore() { return batchAvgScore; }
    public void setBatchAvgScore(double batchAvgScore) { this.batchAvgScore = batchAvgScore; }

    public List<SkillAvgEntry> getSkillAverages() { return skillAverages; }
    public void setSkillAverages(List<SkillAvgEntry> skillAverages) { this.skillAverages = skillAverages; }

    public List<TrainerStudentSkillRow> getStudents() { return students; }
    public void setStudents(List<TrainerStudentSkillRow> students) { this.students = students; }
}