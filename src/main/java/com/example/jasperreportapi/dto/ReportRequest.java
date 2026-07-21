package com.example.jasperreportapi.dto;

import java.util.Map;

public class ReportRequest 
{

    private String referenceNo;
    private String reportName;
    private Map<String, Object> params;

    public ReportRequest() 
    {
    }

    public ReportRequest(String referenceNo) 
    {
        this.referenceNo = referenceNo;
    }

    public String getReferenceNo() 
    {
        return referenceNo;
    }

    public void setReferenceNo(String referenceNo) 
    {
        this.referenceNo = referenceNo;
    }

    public String getReportName() 
    {
        return reportName;
    }

    public void setReportName(String reportName)
    {
        this.reportName = reportName;
    }

    public Map<String, Object> getParams() 
    {
        return params;
    }

    public void setParams(Map<String, Object> params) 
    {
        this.params = params;
    }
}
