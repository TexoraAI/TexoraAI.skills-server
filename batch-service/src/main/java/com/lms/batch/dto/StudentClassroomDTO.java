//
//package com.lms.batch.dto;
//public class StudentClassroomDTO {
//
//    private Long batchId; // ✅ ADD THIS
//
//    private String batchName;
//    private String trainerEmail;
//    private String trainerName;
//
//    public StudentClassroomDTO() {}
//
//    public StudentClassroomDTO(Long batchId, String batchName, String trainerEmail, String trainerName) {
//        this.batchId = batchId;
//        this.batchName = batchName;
//        this.trainerEmail = trainerEmail;
//        this.trainerName = trainerName;
//    }
//
//    public Long getBatchId() { return batchId; }
//    public void setBatchId(Long batchId) { this.batchId = batchId; }
//
//    public String getBatchName() { return batchName; }
//    public void setBatchName(String batchName) { this.batchName = batchName; }
//
//    public String getTrainerEmail() { return trainerEmail; }
//    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }
//
//    public String getTrainerName() { return trainerName; }
//    public void setTrainerName(String trainerName) { this.trainerName = trainerName; }
//}
package com.lms.batch.dto;

import java.io.Serializable;

// OPTIMIZATION: Implements Serializable required for Redis caching (student:classroom cache).
public class StudentClassroomDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long batchId;
    private String batchName;
    private String trainerEmail;
    private String trainerName;

    public StudentClassroomDTO() {}

    public StudentClassroomDTO(Long batchId, String batchName,
                               String trainerEmail, String trainerName) {
        this.batchId      = batchId;
        this.batchName    = batchName;
        this.trainerEmail = trainerEmail;
        this.trainerName  = trainerName;
    }

    public Long getBatchId()                       { return batchId; }
    public void setBatchId(Long batchId)           { this.batchId = batchId; }
    public String getBatchName()                   { return batchName; }
    public void setBatchName(String batchName)     { this.batchName = batchName; }
    public String getTrainerEmail()                { return trainerEmail; }
    public void setTrainerEmail(String v)          { this.trainerEmail = v; }
    public String getTrainerName()                 { return trainerName; }
    public void setTrainerName(String trainerName) { this.trainerName = trainerName; }
}