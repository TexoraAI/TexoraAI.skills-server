package com.lms.progress.dto;

public class StudentProgressReportDTO {

    private String studentEmail;
    private Long batchId;

    // ── Video ──
    private int totalVideos;
    private int videosWatched;
    private double videoWatchPercentage;

    // ── File ──
    private int totalFiles;
    private int filesDownloaded;
    private double fileDownloadPercentage;

    // ── Quiz ──
    private int totalQuizzes;
    private int quizzesCompleted;
    private double quizCompletionPercentage;

    // ── Assignment ──
    private int totalAssignments;
    private int assignmentsCompleted;
    private double assignmentCompletionPercentage;

    // ── Course Content (Progress) ──
    private int totalCourseContent;
    private int courseContentCompleted;
    private double courseProgressPercentage;

    // ── Overall ──
    private double overallProgressPercentage;

    public StudentProgressReportDTO() {}

    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public int getTotalVideos() { return totalVideos; }
    public void setTotalVideos(int totalVideos) { this.totalVideos = totalVideos; }

    public int getVideosWatched() { return videosWatched; }
    public void setVideosWatched(int videosWatched) { this.videosWatched = videosWatched; }

    public double getVideoWatchPercentage() { return videoWatchPercentage; }
    public void setVideoWatchPercentage(double videoWatchPercentage) { this.videoWatchPercentage = videoWatchPercentage; }

    public int getTotalFiles() { return totalFiles; }
    public void setTotalFiles(int totalFiles) { this.totalFiles = totalFiles; }

    public int getFilesDownloaded() { return filesDownloaded; }
    public void setFilesDownloaded(int filesDownloaded) { this.filesDownloaded = filesDownloaded; }

    public double getFileDownloadPercentage() { return fileDownloadPercentage; }
    public void setFileDownloadPercentage(double fileDownloadPercentage) { this.fileDownloadPercentage = fileDownloadPercentage; }

    public int getTotalQuizzes() { return totalQuizzes; }
    public void setTotalQuizzes(int totalQuizzes) { this.totalQuizzes = totalQuizzes; }

    public int getQuizzesCompleted() { return quizzesCompleted; }
    public void setQuizzesCompleted(int quizzesCompleted) { this.quizzesCompleted = quizzesCompleted; }

    public double getQuizCompletionPercentage() { return quizCompletionPercentage; }
    public void setQuizCompletionPercentage(double quizCompletionPercentage) { this.quizCompletionPercentage = quizCompletionPercentage; }

    public int getTotalAssignments() { return totalAssignments; }
    public void setTotalAssignments(int totalAssignments) { this.totalAssignments = totalAssignments; }

    public int getAssignmentsCompleted() { return assignmentsCompleted; }
    public void setAssignmentsCompleted(int assignmentsCompleted) { this.assignmentsCompleted = assignmentsCompleted; }

    public double getAssignmentCompletionPercentage() { return assignmentCompletionPercentage; }
    public void setAssignmentCompletionPercentage(double assignmentCompletionPercentage) { this.assignmentCompletionPercentage = assignmentCompletionPercentage; }

    public int getTotalCourseContent() { return totalCourseContent; }
    public void setTotalCourseContent(int totalCourseContent) { this.totalCourseContent = totalCourseContent; }

    public int getCourseContentCompleted() { return courseContentCompleted; }
    public void setCourseContentCompleted(int courseContentCompleted) { this.courseContentCompleted = courseContentCompleted; }

    public double getCourseProgressPercentage() { return courseProgressPercentage; }
    public void setCourseProgressPercentage(double courseProgressPercentage) { this.courseProgressPercentage = courseProgressPercentage; }

    public double getOverallProgressPercentage() { return overallProgressPercentage; }
    public void setOverallProgressPercentage(double overallProgressPercentage) { this.overallProgressPercentage = overallProgressPercentage; }
}