package com.lms.progress.dto;

import java.util.List;

public class AdminOrgSkillResponse {

    private int    totalStudents;
    private double orgAvgScore;
    private int    totalStrongLearners;
    private int    totalNeedAttention;
    private int    activeBatches;
    private List<SkillAvgEntry>    orgSkillAverages;   // radar + progress bars
    private List<BatchSummaryEntry> batchSummaries;    // batch cards (overview tab)
    private List<BatchSkillMatrix>  batchSkillMatrix;  // by-batch tab bar chart

    public int getTotalStudents() { return totalStudents; }
    public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }

    public double getOrgAvgScore() { return orgAvgScore; }
    public void setOrgAvgScore(double orgAvgScore) { this.orgAvgScore = orgAvgScore; }

    public int getTotalStrongLearners() { return totalStrongLearners; }
    public void setTotalStrongLearners(int totalStrongLearners) { this.totalStrongLearners = totalStrongLearners; }

    public int getTotalNeedAttention() { return totalNeedAttention; }
    public void setTotalNeedAttention(int totalNeedAttention) { this.totalNeedAttention = totalNeedAttention; }

    public int getActiveBatches() { return activeBatches; }
    public void setActiveBatches(int activeBatches) { this.activeBatches = activeBatches; }

    public List<SkillAvgEntry> getOrgSkillAverages() { return orgSkillAverages; }
    public void setOrgSkillAverages(List<SkillAvgEntry> orgSkillAverages) { this.orgSkillAverages = orgSkillAverages; }

    public List<BatchSummaryEntry> getBatchSummaries() { return batchSummaries; }
    public void setBatchSummaries(List<BatchSummaryEntry> batchSummaries) { this.batchSummaries = batchSummaries; }

    public List<BatchSkillMatrix> getBatchSkillMatrix() { return batchSkillMatrix; }
    public void setBatchSkillMatrix(List<BatchSkillMatrix> batchSkillMatrix) { this.batchSkillMatrix = batchSkillMatrix; }
}