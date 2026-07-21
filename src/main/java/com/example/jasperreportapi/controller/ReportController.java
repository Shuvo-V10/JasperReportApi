package com.example.jasperreportapi.controller;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.jasperreportapi.dto.ReportRequest;
import com.example.jasperreportapi.service.ReportService;

import net.sf.jasperreports.engine.JRException;

@RestController
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/reports/generate")
    public ResponseEntity<?> generateReport(@RequestBody ReportRequest request) {
        String reportName = request.getReportName();
        if (reportName == null || reportName.isBlank()) {
            return ResponseEntity.badRequest().body("reportName is required");
        }

        try 
        {
            byte[] pdf = reportService.generateReportPdf(reportName, request.getParams());
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + reportName + ".pdf\"")
            .body(pdf);
        } 
        catch (IOException e) 
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }   
        catch (JRException e) 
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to generate report: " + e.getMessage());
        }
    }

    @PostMapping("/reports/view")
    public ResponseEntity<byte[]> viewDemoReport(@RequestBody ReportRequest request) throws JRException, IOException 
    {
        String referenceNo = request.getReferenceNo();
        byte[] pdf = reportService.generateDemoReportPdf(referenceNo);

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"demo-report.pdf\"")
        .body(pdf);
    }
}
