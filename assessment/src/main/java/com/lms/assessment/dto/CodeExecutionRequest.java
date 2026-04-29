//package com.lms.assessment.dto;
//
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.Pattern;
//
//public class CodeExecutionRequest {
//
//    @NotBlank(message = "batchId is required")
//    private String batchId;
//
//    @NotBlank(message = "Language is required")
//    @Pattern(regexp = "JAVA|PYTHON", message = "Supported languages: JAVA, PYTHON")
//    private String language;
//
//    @NotBlank(message = "Code cannot be empty")
//    private String code;
//
//    // ── Constructors ──────────────────────────────
//    public CodeExecutionRequest() {}
//
//    public CodeExecutionRequest(String batchId, String language, String code) {
//        this.batchId = batchId;
//        this.language = language;
//        this.code = code;
//    }
//
//    // ── Getters ───────────────────────────────────
//    public String getBatchId()  { return batchId; }
//    public String getLanguage() { return language; }
//    public String getCode()     { return code; }
//
//    // ── Setters ───────────────────────────────────
//    public void setBatchId(String batchId)   { this.batchId = batchId; }
//    public void setLanguage(String language) { this.language = language; }
//    public void setCode(String code)         { this.code = code; }
//}


package com.lms.assessment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CodeExecutionRequest {

    @NotBlank(message = "batchId is required")
    private String batchId;

    @NotBlank(message = "Language is required")
//    @Pattern(regexp = "JAVA|PYTHON", message = "Supported languages: JAVA, PYTHON")
   @Pattern(regexp = "JAVA|PYTHON|JAVASCRIPT|MYSQL|BASH",message = "Supported languages: JAVA, PYTHON, JAVASCRIPT,MYSQL,BASH")
    private String language;

    @NotBlank(message = "Code cannot be empty")
    private String code;

    private String sampleInput; // ← ADDED

    // ── Constructors ──────────────────────────────
    public CodeExecutionRequest() {}

    public CodeExecutionRequest(String batchId, String language, String code) {
        this.batchId = batchId;
        this.language = language;
        this.code = code;
    }

    // ── Getters ───────────────────────────────────
    public String getBatchId()     { return batchId; }
    public String getLanguage()    { return language; }
    public String getCode()        { return code; }
    public String getSampleInput() { return sampleInput; } // ← ADDED

    // ── Setters ───────────────────────────────────
    public void setBatchId(String batchId)         { this.batchId = batchId; }
    public void setLanguage(String language)       { this.language = language; }
    public void setCode(String code)               { this.code = code; }
    public void setSampleInput(String sampleInput) { this.sampleInput = sampleInput; } // ← ADDED
}