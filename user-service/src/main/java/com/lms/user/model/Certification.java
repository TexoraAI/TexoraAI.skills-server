package com.lms.user.model;

import jakarta.persistence.*;

@Entity
@Table(name = "certifications")
public class Certification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(name = "cert_name", nullable = false)
    private String certName;

    @Column(name = "issuing_organization")
    private String issuingOrganization;

    @Column(name = "issue_date")
    private String issueDate;

    @Column(name = "expiry_date")
    private String expiryDate;

    @Column(name = "credential_id")
    private String credentialId;

    @Column(name = "credential_url")
    private String credentialUrl;

    @Column(name = "display_order")
    private Integer displayOrder;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Resume getResume() { return resume; }
    public void setResume(Resume resume) { this.resume = resume; }

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