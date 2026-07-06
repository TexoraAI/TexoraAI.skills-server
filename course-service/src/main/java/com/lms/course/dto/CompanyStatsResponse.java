package com.lms.course.dto;

public class CompanyStatsResponse {

    private long total;
    private long techPartners;
    private long businessPartners;
    private long texoraProducts;
    private long active;

    public CompanyStatsResponse() {
    }

    public CompanyStatsResponse(long total, long techPartners, long businessPartners, long texoraProducts, long active) {
        this.total = total;
        this.techPartners = techPartners;
        this.businessPartners = businessPartners;
        this.texoraProducts = texoraProducts;
        this.active = active;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getTechPartners() {
        return techPartners;
    }

    public void setTechPartners(long techPartners) {
        this.techPartners = techPartners;
    }

    public long getBusinessPartners() {
        return businessPartners;
    }

    public void setBusinessPartners(long businessPartners) {
        this.businessPartners = businessPartners;
    }

    public long getTexoraProducts() {
        return texoraProducts;
    }

    public void setTexoraProducts(long texoraProducts) {
        this.texoraProducts = texoraProducts;
    }

    public long getActive() {
        return active;
    }

    public void setActive(long active) {
        this.active = active;
    }
}