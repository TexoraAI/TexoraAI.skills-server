package com.lms.progress.dto;

import java.util.List;

public class BatchProgressReportDTO {

    private Long batchId;
    private int totalStudents;

    // batch-level averages (average of all students in this batch)
    private double avgVideoWatchPercentage;
    private double avgFileDownloadPercentage;
    private double avgQuizCompletionPercentage;
    private double avgAssignmentCompletionPercentage;
    private double avgCourseProgressPercentage;
    private double avgOverallProgressPercentage;

    private List<StudentProgressReportDTO> studentReports;

    public BatchProgressReportDTO() {}

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public int getTotalStudents() { return totalStudents; }
    public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }

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

    public List<StudentProgressReportDTO> getStudentReports() { return studentReports; }
    public void setStudentReports(List<StudentProgressReportDTO> studentReports) { this.studentReports = studentReports; }
}