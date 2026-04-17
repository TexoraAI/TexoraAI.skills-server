package com.lms.progress.dto;

import java.util.List;

public class TrainerProgressReportDTO {

    private String trainerEmail;
    private int totalBatches;
    private long totalStudentsHandled;

    // averages across all batches this trainer owns
    private double avgVideoWatchPercentage;
    private double avgFileDownloadPercentage;
    private double avgQuizCompletionPercentage;
    private double avgAssignmentCompletionPercentage;
    private double avgCourseProgressPercentage;
    private double avgOverallProgressPercentage;

    private List<BatchProgressReportDTO> batchReports;

    public TrainerProgressReportDTO() {}

    public String getTrainerEmail() { return trainerEmail; }
    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }

    public int getTotalBatches() { return totalBatches; }
    public void setTotalBatches(int totalBatches) { this.totalBatches = totalBatches; }

    public long getTotalStudentsHandled() { return totalStudentsHandled; }
    public void setTotalStudentsHandled(long totalStudentsHandled) { this.totalStudentsHandled = totalStudentsHandled; }

    public double getAvgVideoWatchPercentage() { return avgVideoWatchPercentage; }
    public void setAvgVideoWatchPercentage(double avgVideoWatchPercentage) { this.avgVideoWatchPercentage = avgVideoWatchPercentage; }

    public double getAvgFileDownloadPercentage() { return avgFileDownloadPercentage; }
    public void setAvgFileDownloadPercentage(double avgFileDownloadPercentage) { this.avgFileDownloadPercentage = avgFileDownloadPercentage; }

    public double getAvgQuizCompletionPercentage() { return avgQuizCompletionPercentage; }
    public void setAvgQuizCompletionPercentage(double avgQuizCompletionPercentage) { this.avgQuizCompletionPercentage = avgQuizCompletionPercentage; }

    public double getAvgAssignmentCompletionPercentage() { return avgAssignmentCompletionPercentage; }
    public void setAvgAssignmentCompletionPercentage(double avgAssignmentCompletionPercentage) { this.avgAssignmentCompletionPercentage = avgAssignmentCompletionPercentage; }

    public double getAvgCourseProgressPercentage() { return avgCourseProgressPercentage; }
    public void setAvgCourseProgressPercentage(double avgCourseProgressPercentage) { this.avgCourseProgressPercentage = avgCourseProgressPercentage; }

    public double getAvgOverallProgressPercentage() { return avgOverallProgressPercentage; }
    public void setAvgOverallProgressPercentage(double avgOverallProgressPercentage) { this.avgOverallProgressPercentage = avgOverallProgressPercentage; }

    public List<BatchProgressReportDTO> getBatchReports() { return batchReports; }
    public void setBatchReports(List<BatchProgressReportDTO> batchReports) { this.batchReports = batchReports; }
}