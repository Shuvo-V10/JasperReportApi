package com.example.jasperreportapi.controller;

import org.springframework.http.HttpHeaders;
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

    // The ReportController requires a ReportService to function. Spring automatically injects the ReportService bean into the ReportController via its constructor
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

     @PostMapping("/reports/view") 
    public ResponseEntity<byte[]> viewDemoReport(@RequestBody ReportRequest request) throws JRException {
        
        // Extract the referenceNo from the JSON body object
        String referenceNo = request.getReferenceNo();
        
        // Call the service (Notice how the service method remains exactly the same!)
        byte[] pdf = reportService.generateDemoReportPdf(referenceNo);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"demo-report.pdf\"")
                .body(pdf);
    }
}