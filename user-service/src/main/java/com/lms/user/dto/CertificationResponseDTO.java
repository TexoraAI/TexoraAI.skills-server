package com.lms.user.dto;

public class CertificationResponseDTO {

    private Long id;
    private String certName;
    private String issuingOrganization;
    private String issueDate;
    private String expiryDate;
    private String credentialId;
    private String credentialUrl;
    private Integer displayOrder;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCertName() { return certName; }
    public void setCertName(String certName) { this.certName = certName; }

    public String getIssuingOrganization() { return issuingOrganization; }
    public void setIssuingOrganization(String issuingOrganization) { this.issuingOrganization = issuingOrganization; }

    public String getIssueDate() { return issueDate; }
    public void setIssueDate(String issueDate) { this.issueDate = issueDate; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public String getCredentialId() { return credentialId; }
    public void setCredentialId(String credentialId) { this.credentialId = credentialId; }

    public String getCredentialUrl() { return credentialUrl; }
    public void setCredentialUrl(String credentialUrl) { this.credentialUrl = credentialUrl; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
}