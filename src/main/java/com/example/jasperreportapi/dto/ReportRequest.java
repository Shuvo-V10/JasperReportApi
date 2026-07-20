package com.example.jasperreportapi.dto;

public class ReportRequest {
    
    private String referenceNo;

    // Default constructor is required for Jackson (JSON parser) to instantiate the object
    public ReportRequest() {
    }

    // Constructor with parameters (optional but useful)
    public ReportRequest(String referenceNo) {
        this.referenceNo = referenceNo;
    }

    public String getReferenceNo() {
        return referenceNo;
    }

    public void setReferenceNo(String referenceNo) {
        this.referenceNo = referenceNo;
    }
}