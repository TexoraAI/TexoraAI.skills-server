package com.lms.chat.dto;

import com.lms.chat.entity.NotebookStudioOutput;
import java.time.LocalDateTime;

public class NotebookStudioOutputResponse {

    private Long id;
    private String type;
    private String language;
    private String textContent;
    private String s3Key;
    private String downloadUrl;
    private String status;
    private String errorMessage;
    private LocalDateTime createdAt;

    public static NotebookStudioOutputResponse from(NotebookStudioOutput output, String downloadUrl) {
        NotebookStudioOutputResponse res = new NotebookStudioOutputResponse();
        res.id = output.getId();
        res.type = output.getType() != null ? output.getType().name() : null;
        res.language = output.getLanguage();
        res.textContent = output.getTextContent();
        res.s3Key = output.getS3Key();
        res.downloadUrl = downloadUrl;
        res.status = output.getStatus() != null ? output.getStatus().name() : null;
        res.errorMessage = output.getErrorMessage();
        res.createdAt = output.getCreatedAt();
        return res;
    }

    // Read-only response DTO — getters only
    public Long getId() { return id; }
    public String getType() { return type; }
    public String getLanguage() { return language; }
    public String getTextContent() { return textContent; }
    public String getS3Key() { return s3Key; }
    public String getDownloadUrl() { return downloadUrl; }
    public String getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}