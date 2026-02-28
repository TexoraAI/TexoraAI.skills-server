package com.lms.batch.dto;

import java.util.List;

public class AssignStudentsRequest {

    private List<String> studentEmails;

    public List<String> getStudentEmails() {
        return studentEmails;
    }

    public void setStudentEmails(List<String> studentEmails) {
        this.studentEmails = studentEmails;
    }
}
