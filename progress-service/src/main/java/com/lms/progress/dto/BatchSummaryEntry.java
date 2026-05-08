package com.lms.progress.dto;

public class BatchSummaryEntry {

    private Long   batchId;
    private String trainerEmail;
    private int    students;
    private double avgScore;
    private int    strongCount;
    private int    weakCount;

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getTrainerEmail() { return trainerEmail; }
    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }

    public int getStudents() { return students; }
    public void setStudents(int students) { this.students = students; }

    public double getAvgScore() { return avgScore; }
    public void setAvgScore(double avgScore) { this.avgScore = avgScore; }

    public int getStrongCount() { return strongCount; }
    public void setStrongCount(int strongCount) { this.strongCount = strongCount; }

    public int getWeakCount() { return weakCount; }
    public void setWeakCount(int weakCount) { this.weakCount = weakCount; }
}