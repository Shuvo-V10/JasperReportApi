package com.example.jasperreportapi.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
    private final String reportsDirectory;

    public ReportService(DataSource dataSource, @Value("${reports.directory:./reports}") String reportsDirectory) 
    {
        this.dataSource = dataSource;
        this.reportsDirectory = reportsDirectory;
    }

    public byte[] generateReportPdf(String reportName, Map<String, Object> params)throws JRException, IOException 
    {

        // 1. Convert to File to get the Absolute Path (fixes working directory issues)
        File reportsDirFile = new File(reportsDirectory);   

        // Optional: Add a check to ensure the directory actually exists
        if (!reportsDirFile.exists() || !reportsDirFile.isDirectory()) {
            throw new FileNotFoundException("Reports directory not found at absolute path: " + reportsDirFile.getAbsolutePath());
        }

        Path jrxmlPath = Paths.get(reportsDirFile.getAbsolutePath(), reportName + ".jrxml");

        if (!jrxmlPath.toFile().exists()) 
        {
            throw new FileNotFoundException("Report not found: " + jrxmlPath.toAbsolutePath());
        }

        if (params == null) 
        {
            params = new HashMap<>();
        }

          // 2. Use Absolute Path and ensure it ends with a forward slash "/"
        String absoluteSubReportDir = reportsDirFile.getAbsolutePath().replace("\\", "/") + "/";
        params.put("SUBREPORT_DIRECTORY", absoluteSubReportDir);  

        //params.put("SUBREPORT_DIR", reportsDirectory + File.separator);   // <-- new

        System.out.println("Looking for subreports in: " + params.get("SUBREPORT_DIRECTORY"));
        System.out.println("Full expected path: " + params.get("SUBREPORT_DIRECTORY") + "SubReport.jasper");



        try (InputStream reportStream = new FileInputStream(jrxmlPath.toFile()))
        {
            JasperReport compiledReport = JasperCompileManager.compileReport(reportStream);

            try (Connection connection = dataSource.getConnection()) 
            {
                JasperPrint jasperPrint = JasperFillManager.fillReport(compiledReport, params, connection);

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                JRPdfExporter exporter = new JRPdfExporter();
                exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
                exporter.exportReport();

                return outputStream.toByteArray();
            } 
            catch (SQLException e) 
            {
                throw new JRException("Failed to obtain database connection for report", e);
            }
        }
    }

    public byte[] generateDemoReportPdf(String referenceNo) throws JRException, IOException 
    {
        return generateReportPdf("DemoReport", Map.of("ReferenceNo", referenceNo));
    }
}
