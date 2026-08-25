package com.securityintel.controller;

import com.securityintel.exception.DatabaseException;
import com.securityintel.model.Environment;
import com.securityintel.model.ScanExecution;
import com.securityintel.model.TriggerType;
import com.securityintel.parser.SecurityReportParseException;
import com.securityintel.scan.ScanExecutionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/integrations/scans/github-actions")
@CrossOrigin(origins = "*")
public class GitHubActionsIntegrationController {

    @Value("${scan.ingestion.token:}")
    private String scanIngestionToken;

    private final ScanExecutionService scanExecutionService;

    public GitHubActionsIntegrationController(ScanExecutionService scanExecutionService) {
        this.scanExecutionService = scanExecutionService;
    }

    @PostMapping
    public ResponseEntity<?> ingestScanFromGitHubActions(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("file") MultipartFile file,
            @RequestParam("serviceName") String serviceName,
            @RequestParam("repository") String repository,
            @RequestParam("branch") String branch,
            @RequestParam("commitId") String commitId,
            @RequestParam("workflowRunId") String workflowRunId,
            @RequestParam("tool") String tool,
            @RequestParam("scanType") String scanType,
            @RequestParam(value = "environment", defaultValue = "PRODUCTION") String environment) {

        // Validate authentication
        if (scanIngestionToken == null || scanIngestionToken.isEmpty()) {
            return ResponseEntity.status(500).body("SCAN_INGESTION_TOKEN not configured");
        }

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing or invalid Authorization header");
        }

        String token = authorization.substring(7); // Remove "Bearer " prefix
        if (!token.equals(scanIngestionToken)) {
            return ResponseEntity.status(403).body("Invalid authentication token");
        }

        try {
            // Parse tool and scan type
            com.securityintel.model.Tool toolEnum = com.securityintel.model.Tool.valueOf(tool.toUpperCase());
            com.securityintel.model.ScanType scanTypeEnum = com.securityintel.model.ScanType.valueOf(scanType.toUpperCase());
            Environment environmentEnum = Environment.valueOf(environment.toUpperCase());

            // Process the scan
            ScanExecution scanExecution = scanExecutionService.processScanExecution(
                file,
                serviceName,
                environmentEnum,
                TriggerType.GITHUB_ACTIONS,
                repository,
                branch,
                commitId,
                workflowRunId
            );

            return ResponseEntity.ok(scanExecution);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid tool or scan type: " + e.getMessage());
        } catch (com.securityintel.parser.SecurityReportParseException e) {
            return ResponseEntity.badRequest().body("Failed to parse security report: " + e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to read uploaded file: " + e.getMessage());
        } catch (DatabaseException e) {
            return ResponseEntity.status(500).body("Database error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        if (scanIngestionToken == null || scanIngestionToken.isEmpty()) {
            return ResponseEntity.status(500).body("SCAN_INGESTION_TOKEN not configured");
        }
        return ResponseEntity.ok("GitHub Actions integration is ready");
    }
}