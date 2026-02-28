package com.lms.assessment.dto;

public class FileUploadResponse {

    private Long id;
    private String fileName;
    private String downloadUrl;

    public FileUploadResponse(Long id, String fileName, String downloadUrl) {
        this.id = id;
        this.fileName = fileName;
        this.downloadUrl = downloadUrl;
    }

    public Long getId() { return id; }
    public String getFileName() { return fileName; }
    public String getDownloadUrl() { return downloadUrl; }
}
