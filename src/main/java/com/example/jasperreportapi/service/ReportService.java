package com.example.jasperreportapi.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.pdf.JRPdfExporter;

@Service
public class ReportService {

    private final DataSource dataSource;
    private JasperReport demoReport;

    public ReportService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void init() throws IOException, JRException {

        System.setProperty("net.sf.jasperreports.query.executer.factory.plsql","net.sf.jasperreports.engine.query.PlSqlQueryExecuterFactory");

        try (InputStream reportStream = new ClassPathResource("reports/DemoReport.jrxml").getInputStream()) {
            
            this.demoReport = JasperCompileManager.compileReport(reportStream);
        }
    }

    public byte[] generateDemoReportPdf(String referenceNo) throws JRException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("ReferenceNo", referenceNo); // key must match the parameter name in Studio exactly, case-sensitive

        try (Connection connection = dataSource.getConnection()) {
            JasperPrint jasperPrint = JasperFillManager.fillReport(demoReport, parameters, connection);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
            exporter.exportReport();

            return outputStream.toByteArray();
        } catch (SQLException e) {
            throw new JRException("Failed to obtain database connection for report", e);
        }
    }
}