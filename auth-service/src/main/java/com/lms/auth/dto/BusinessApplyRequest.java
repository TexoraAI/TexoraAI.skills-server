package com.lms.auth.dto;

import com.lms.auth.model.BusinessType;
import com.lms.auth.model.CompanySize;
import com.lms.auth.model.IndustryDomain;

import java.util.List;

public class BusinessApplyRequest {

    // Step-1
    private String businessName;
    private String ownerName;
    private String email;
    private String mobileNumber;

    private BusinessType businessType;
    private IndustryDomain industryDomain;

    private String location;
    private String website;

    // Step-2
    private CompanySize companySize;
    private Integer yearsOfExperience;
    private List<String> lookingFor; // Trainers, Students, Courses, Corporate Training
    private String aboutBusiness;
    private String expectedOutcome;

    // Step-3
    private String password;

    public BusinessApplyRequest() {
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public BusinessType getBusinessType() {
        return businessType;
    }

    public void setBusinessType(BusinessType businessType) {
        this.businessType = businessType;
    }

    public IndustryDomain getIndustryDomain() {
        return industryDomain;
    }

    public void setIndustryDomain(IndustryDomain industryDomain) {
        this.industryDomain = industryDomain;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public CompanySize getCompanySize() {
        return companySize;
    }

    public void setCompanySize(CompanySize companySize) {
        this.companySize = companySize;
    }

    public Integer getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(Integer yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public List<String> getLookingFor() {
        return lookingFor;
    }

    public void setLookingFor(List<String> lookingFor) {
        this.lookingFor = lookingFor;
    }

    public String getAboutBusiness() {
        return aboutBusiness;
    }

    public void setAboutBusiness(String aboutBusiness) {
        this.aboutBusiness = aboutBusiness;
    }

    public String getExpectedOutcome() {
        return expectedOutcome;
    }

    public void setExpectedOutcome(String expectedOutcome) {
        this.expectedOutcome = expectedOutcome;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
