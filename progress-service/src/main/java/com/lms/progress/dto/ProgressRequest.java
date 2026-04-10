//package com.lms.progress.dto;
//
//import java.util.List;
//
//public class ProgressRequest {
//
//    private String studentEmail;   // ✅ FIXED
//    private Long courseId;
//    private List<Long> completedContentIds;
//    private double progressPercentage;
//
//    // ---- getters ----
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
//    // ---- setters ----
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
//}
package com.lms.progress.dto;

import java.util.List;

public class ProgressRequest {

    private String     studentEmail;
    private Long       courseId;
    private List<Long> completedContentIds;
    private double     progressPercentage;
    private int        totalContentCount;   // ✅ ADDED

    public String     getStudentEmail()            { return studentEmail; }
    public Long       getCourseId()                { return courseId; }
    public List<Long> getCompletedContentIds()     { return completedContentIds; }
    public double     getProgressPercentage()      { return progressPercentage; }
    public int        getTotalContentCount()       { return totalContentCount; } // ✅ ADDED

    public void setStudentEmail(String studentEmail)             { this.studentEmail = studentEmail; }
    public void setCourseId(Long courseId)                       { this.courseId = courseId; }
    public void setCompletedContentIds(List<Long> ids)           { this.completedContentIds = ids; }
    public void setProgressPercentage(double progressPercentage) { this.progressPercentage = progressPercentage; }
    public void setTotalContentCount(int totalContentCount)      { this.totalContentCount = totalContentCount; } // ✅ ADDED
}