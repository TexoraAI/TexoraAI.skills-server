//package com.lms.progress.dto;
//
//import java.time.Instant;
//import java.util.List;
//
//public class ProgressResponse {
//
//    private Long progressId;
//    private String studentEmail;   // ✅ FIXED
//    private Long courseId;
//    private List<Long> completedContentIds;
//    private double progressPercentage;
//    private Instant updatedAt;
//
//    // ---- getters ----
//    public Long getProgressId() {
//        return progressId;
//    }
//
//    public String getStudentEmail() {
//        return studentEmail;
//    }
//
//    public Long getCourseId() {
//        return courseId;
//    }
//
//    public List<Long> getCompletedContentIds() {
//        return completedContentIds;
//    }
//
//    public double getProgressPercentage() {
//        return progressPercentage;
//    }
//
//    public Instant getUpdatedAt() {
//        return updatedAt;
//    }
//
//    // ---- setters ----
//    public void setProgressId(Long progressId) {
//        this.progressId = progressId;
//    }
//
//    public void setStudentEmail(String studentEmail) {
//        this.studentEmail = studentEmail;
//    }
//
//    public void setCourseId(Long courseId) {
//        this.courseId = courseId;
//    }
//
//    public void setCompletedContentIds(List<Long> completedContentIds) {
//        this.completedContentIds = completedContentIds;
//    }
//
//    public void setProgressPercentage(double progressPercentage) {
//        this.progressPercentage = progressPercentage;
//    }
//
//    public void setUpdatedAt(Instant updatedAt) {
//        this.updatedAt = updatedAt;
//    }
//}

package com.lms.progress.dto;

import java.time.Instant;
import java.util.List;

public class ProgressResponse {

    private Long       progressId;
    private String     studentEmail;
    private Long       courseId;
    private List<Long> completedContentIds;
    private double     progressPercentage;
    private int        totalContentCount;   // ✅ ADDED
    private Instant    updatedAt;

    public Long       getProgressId()              { return progressId; }
    public String     getStudentEmail()            { return studentEmail; }
    public Long       getCourseId()                { return courseId; }
    public List<Long> getCompletedContentIds()     { return completedContentIds; }
    public double     getProgressPercentage()      { return progressPercentage; }
    public int        getTotalContentCount()       { return totalContentCount; } // ✅ ADDED
    public Instant    getUpdatedAt()               { return updatedAt; }

    public void setProgressId(Long progressId)                   { this.progressId = progressId; }
    public void setStudentEmail(String studentEmail)             { this.studentEmail = studentEmail; }
    public void setCourseId(Long courseId)                       { this.courseId = courseId; }
    public void setCompletedContentIds(List<Long> ids)           { this.completedContentIds = ids; }
    public void setProgressPercentage(double progressPercentage) { this.progressPercentage = progressPercentage; }
    public void setTotalContentCount(int totalContentCount)      { this.totalContentCount = totalContentCount; } // ✅ ADDED
    public void setUpdatedAt(Instant updatedAt)                  { this.updatedAt = updatedAt; }
}