package com.example.jasperreportapi.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.jasperreportapi.service.ReportService;

import net.sf.jasperreports.engine.JRException;

@RestController
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/api/reports/demo")
    public ResponseEntity<byte[]> viewDemoReport(@RequestParam String referenceNo) throws JRException {
        byte[] pdf = reportService.generateDemoReportPdf(referenceNo);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"demo-report.pdf\"")
                .body(pdf);
    }
}