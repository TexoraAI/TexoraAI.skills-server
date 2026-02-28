package com.lms.file.dto;

public class CertificateResponse {

    private String fileName;
    private String courseName;
    private String type;
    private String issuedDate;

    public CertificateResponse() {}

    public CertificateResponse(
            String fileName,
            String courseName,
            String type,
            String issuedDate) {

        this.fileName = fileName;
        this.courseName = courseName;
        this.type = type;
        this.issuedDate = issuedDate;
    }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getIssuedDate() { return issuedDate; }
    public void setIssuedDate(String issuedDate) { this.issuedDate = issuedDate; }
}
