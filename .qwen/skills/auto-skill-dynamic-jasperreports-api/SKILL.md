---
name: dynamic-jasperreports-api
description: Convert a hardcoded single-report Spring Boot JasperReports API into a dynamic multi-report service with external report directory, flexible parameters, and compile-on-request.
source: auto-skill
extracted_at: '2026-07-21T10:08:17.233Z'
---

## Dynamic JasperReports API — Conversion Pattern

When a Spring Boot JasperReports API is hardcoded for a single report (compiled at startup via `@PostConstruct`, fixed endpoint, fixed parameters), convert it to support hundreds of reports dynamically using this pattern.

### Design decisions to confirm with user first

1. **Routing** — Single endpoint with `reportName` in body vs. per-report endpoints vs. path-based routing
2. **Storage** — Bundled in JAR resources vs. external filesystem directory vs. database blobs
3. **Parameters** — Flexible `Map<String, Object>` vs. validated schema per report
4. **Caching** — Compile-on-request vs. lazy compile + cache with eviction

### Implementation

#### 1. `application.properties` — add configurable reports directory
```properties
reports.directory=${REPORTS_DIR:./reports}
```

#### 2. `ReportRequest.java` — add dynamic fields
Keep existing fields for backward compatibility. Add:
```java
private String reportName;
private Map<String, Object> params;
// getters and setters
```

#### 3. `ReportService.java` — refactor
- Remove `@PostConstruct init()` and any cached `JasperReport` field
- Inject `reports.directory` via `@Value`
- Replace single-report method with a generic one:
```java
public byte[] generateReportPdf(String reportName, Map<String, Object> params)
        throws JRException, IOException {
    Path jrxmlPath = Paths.get(reportsDirectory, reportName + ".jrxml");
    if (!jrxmlPath.toFile().exists()) {
        throw new FileNotFoundException("Report not found: " + jrxmlPath);
    }
    try (InputStream reportStream = new FileInputStream(jrxmlPath.toFile())) {
        JasperReport compiledReport = JasperCompileManager.compileReport(reportStream);
        try (Connection connection = dataSource.getConnection()) {
            JasperPrint jasperPrint = JasperFillManager.fillReport(compiledReport, params, connection);
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
```
- Keep old method as a thin delegator for backward compatibility:
```java
public byte[] generateDemoReportPdf(String referenceNo) throws JRException, IOException {
    return generateReportPdf("DemoReport", Map.of("ReferenceNo", referenceNo));
}
```

#### 4. `ReportController.java` — add dynamic endpoint
```java
@PostMapping("/reports/generate")
public ResponseEntity<?> generateReport(@RequestBody ReportRequest request) {
    String reportName = request.getReportName();
    if (reportName == null || reportName.isBlank()) {
        return ResponseEntity.badRequest().body("reportName is required");
    }
    try {
        byte[] pdf = reportService.generateReportPdf(reportName, request.getParams());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + reportName + ".pdf\"")
                .body(pdf);
    } catch (IOException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    } catch (JRException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to generate report: " + e.getMessage());
    }
}
```
- Keep the old endpoint for backward compatibility

### Key considerations

- **Exception types**: `FileInputStream` close throws `IOException` in try-with-resources — declare it, don't use `FileNotFoundException` for the catch block in the controller (since `FileNotFoundException` is a subclass of `IOException`, catching `IOException` covers both)
- **No startup compilation**: Remove `@PostConstruct` entirely — each request compiles fresh. This avoids stale compiled reports when `.jrxml` files change
- **Docker**: The `reports.directory` can be a mounted volume: `-v /host/reports:/app/reports -e REPORTS_DIR=/app/reports`
- **Adding new reports**: Drop a `.jrxml` into the reports directory — no code changes, no rebuild needed
