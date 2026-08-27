package com.securityintel.controller;

import com.securityintel.dto.ScanReportDto;
import com.securityintel.model.Environment;
import com.securityintel.service.SecurityReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final SecurityReportService securityReportService;

    public ReportController(SecurityReportService securityReportService) {
        this.securityReportService = securityReportService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadReport(@RequestParam("file") MultipartFile file,
                                        @RequestParam("serviceName") String serviceName,
                                        @RequestParam("environment") Environment environment) {
        try {
            SecurityReportService.ReportProcessingResult result = 
                securityReportService.processSecurityReport(file, serviceName, environment);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Report processed successfully",
                "tool", result.getTool().name(),
                "serviceName", serviceName,
                "rawFindings", result.getRawFindings(),
                "uniqueFindings", result.getUniqueFindings(),
                "report", result.getScanReport()
            );
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "message", "Failed to process report: " + e.getMessage()
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<List<ScanReportDto>> getAllReports() {
        List<ScanReportDto> reports = securityReportService.getAllReports();
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScanReportDto> getReportById(@PathVariable String id) {
        ScanReportDto report = securityReportService.getReportById(id);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadReport(@PathVariable String id) {
        SecurityReportService.ScanReportDownload report = securityReportService.getReportDownload(id);
        String fileName = report.fileName() == null || report.fileName().isBlank()
            ? "security-report.json" : report.fileName();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName.replace("\"", "") + "\"")
            .contentType(MediaType.APPLICATION_JSON)
            .body(report.content().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    @GetMapping("/service/{serviceName}")
    public ResponseEntity<List<ScanReportDto>> getReportsByService(@PathVariable String serviceName) {
        List<ScanReportDto> reports = securityReportService.getReportsByService(serviceName);
        return ResponseEntity.ok(reports);
    }
}