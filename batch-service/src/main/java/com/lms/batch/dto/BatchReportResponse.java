package com.lms.batch.dto;

import java.util.List;

public class BatchReportResponse {

    private int totalBatches;
    private int totalStudents;
    private double avgScore;
    private int topBatches;

    // optional – for future UI expansion
    private List<TrainerBatchReportDTO> batches;

    public int getTotalBatches() {
        return totalBatches;
    }

    public void setTotalBatches(int totalBatches) {
        this.totalBatches = totalBatches;
    }

    public int getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(int totalStudents) {
        this.totalStudents = totalStudents;
    }

    public double getAvgScore() {
        return avgScore;
    }

    public void setAvgScore(double avgScore) {
        this.avgScore = avgScore;
    }

    public int getTopBatches() {
        return topBatches;
    }

    public void setTopBatches(int topBatches) {
        this.topBatches = topBatches;
    }

    public List<TrainerBatchReportDTO> getBatches() {
        return batches;
    }

    public void setBatches(List<TrainerBatchReportDTO> batches) {
        this.batches = batches;
    }
}
