package com.lms.attendance.dto;

import com.lms.attendance.entity.TrainerSessionAttendance;

import java.util.List;
import java.util.Map;

public class BatchAttendanceDetailResponse {

    private List<TrainerSessionAttendance> trainerAttendance;
    // studentEmail -> list of {date, status} rows
    private Map<String, List<StudentAttendanceResponse>> studentAttendance;

    public BatchAttendanceDetailResponse() {}

    public BatchAttendanceDetailResponse(
            List<TrainerSessionAttendance> trainerAttendance,
            Map<String, List<StudentAttendanceResponse>> studentAttendance) {
        this.trainerAttendance = trainerAttendance;
        this.studentAttendance = studentAttendance;
    }

    public List<TrainerSessionAttendance> getTrainerAttendance() { return trainerAttendance; }
    public void setTrainerAttendance(List<TrainerSessionAttendance> trainerAttendance) { this.trainerAttendance = trainerAttendance; }

    public Map<String, List<StudentAttendanceResponse>> getStudentAttendance() { return studentAttendance; }
    public void setStudentAttendance(Map<String, List<StudentAttendanceResponse>> studentAttendance) { this.studentAttendance = studentAttendance; }
}