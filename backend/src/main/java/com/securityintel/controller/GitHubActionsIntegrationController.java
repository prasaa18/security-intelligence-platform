package com.securityintel.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.securityintel.exception.DatabaseException;
import com.securityintel.model.Environment;
import com.securityintel.model.ScanExecution;
import com.securityintel.model.TriggerType;
import com.securityintel.parser.SecurityReportParseException;
import com.securityintel.scan.ScanExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/integrations/scans/github-actions")
@CrossOrigin(origins = "*")
public class GitHubActionsIntegrationController {

    private static final Logger log = LoggerFactory.getLogger(GitHubActionsIntegrationController.class);

    @Value("${scan.ingestion.token:test-token-123}")
    private String scanIngestionToken;

    private final ScanExecutionService scanExecutionService;
    private final ObjectMapper objectMapper;

    public GitHubActionsIntegrationController(ScanExecutionService scanExecutionService, ObjectMapper objectMapper) {
        this.scanExecutionService = scanExecutionService;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    public ResponseEntity<?> ingestScanFromGitHubActions(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "token", required = false) String tokenParam,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "metadata", required = false) MultipartFile metadataFile,
            @RequestParam(value = "metadataJson", required = false) String metadataJson,
            @RequestParam(value = "serviceName", required = false) String serviceName,
            @RequestParam(value = "repository", required = false) String repository,
            @RequestParam(value = "branch", required = false) String branch,
            @RequestParam(value = "commitId", required = false) String commitId,
            @RequestParam(value = "commit", required = false) String commit,
            @RequestParam(value = "workflowRunId", required = false) String workflowRunId,
            @RequestParam(value = "tool", required = false) String tool,
            @RequestParam(value = "scanType", required = false) String scanType,
            @RequestParam(value = "environment", required = false, defaultValue = "PRODUCTION") String environment) {

        // Validate authentication
        String providedToken = null;
        if (authorization != null && !authorization.isBlank()) {
            if (authorization.toLowerCase().startsWith("bearer ")) {
                providedToken = authorization.substring(7).trim();
            } else {
                providedToken = authorization.trim();
            }
        } else if (tokenParam != null && !tokenParam.isBlank()) {
            providedToken = tokenParam.trim();
        }

        if (providedToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "success", false,
                "error", "Unauthorized",
                "message", "Missing Authorization header or token parameter"
            ));
        }

        if (scanIngestionToken != null && !scanIngestionToken.isBlank() && !scanIngestionToken.equals(providedToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "success", false,
                "error", "Forbidden",
                "message", "Invalid scan ingestion token"
            ));
        }

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Bad Request",
                "message", "No scan report file provided. Use form field 'file=@report.json'"
            ));
        }

        // Parse metadata if provided
        String effectiveServiceName = serviceName;
        String effectiveRepository = repository;
        String effectiveBranch = branch;
        String effectiveCommitId = commitId != null ? commitId : commit;
        String effectiveWorkflowRunId = workflowRunId;
        String effectiveEnvironment = environment;

        try {
            JsonNode metaNode = null;
            if (metadataFile != null && !metadataFile.isEmpty()) {
                metaNode = objectMapper.readTree(metadataFile.getBytes());
            } else if (metadataJson != null && !metadataJson.isBlank()) {
                metaNode = objectMapper.readTree(metadataJson);
            }

            if (metaNode != null) {
                if ((effectiveServiceName == null || effectiveServiceName.isBlank()) && metaNode.has("serviceName")) {
                    effectiveServiceName = metaNode.get("serviceName").asText();
                }
                if ((effectiveRepository == null || effectiveRepository.isBlank()) && metaNode.has("repository")) {
                    effectiveRepository = metaNode.get("repository").asText();
                }
                if ((effectiveBranch == null || effectiveBranch.isBlank()) && metaNode.has("branch")) {
                    effectiveBranch = metaNode.get("branch").asText();
                }
                if ((effectiveCommitId == null || effectiveCommitId.isBlank())) {
                    if (metaNode.has("commitId")) {
                        effectiveCommitId = metaNode.get("commitId").asText();
                    } else if (metaNode.has("commit")) {
                        effectiveCommitId = metaNode.get("commit").asText();
                    }
                }
                if ((effectiveWorkflowRunId == null || effectiveWorkflowRunId.isBlank()) && metaNode.has("workflowRunId")) {
                    effectiveWorkflowRunId = metaNode.get("workflowRunId").asText();
                }
                if (metaNode.has("environment") && (environment == null || "PRODUCTION".equalsIgnoreCase(environment))) {
                    effectiveEnvironment = metaNode.get("environment").asText();
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse metadata file/json: {}", e.getMessage());
        }

        // Fallbacks for metadata
        if (effectiveServiceName == null || effectiveServiceName.isBlank()) {
            if (effectiveRepository != null && !effectiveRepository.isBlank()) {
                effectiveServiceName = effectiveRepository.contains("/")
                    ? effectiveRepository.substring(effectiveRepository.lastIndexOf('/') + 1)
                    : effectiveRepository;
            } else {
                effectiveServiceName = "default-service";
            }
        }

        if (effectiveRepository == null || effectiveRepository.isBlank()) {
            effectiveRepository = effectiveServiceName;
        }
        if (effectiveBranch == null || effectiveBranch.isBlank()) {
            effectiveBranch = "main";
        }
        if (effectiveCommitId == null || effectiveCommitId.isBlank()) {
            effectiveCommitId = "HEAD";
        }
        if (effectiveWorkflowRunId == null || effectiveWorkflowRunId.isBlank()) {
            effectiveWorkflowRunId = "run-" + System.currentTimeMillis();
        }

        Environment environmentEnum = Environment.PRODUCTION;
        try {
            if (effectiveEnvironment != null) {
                environmentEnum = Environment.valueOf(effectiveEnvironment.toUpperCase().trim());
            }
        } catch (IllegalArgumentException e) {
            environmentEnum = Environment.PRODUCTION;
        }

        try {
            ScanExecution scanExecution = scanExecutionService.processScanExecution(
                file,
                effectiveServiceName,
                environmentEnum,
                TriggerType.GITHUB_ACTIONS,
                effectiveRepository,
                effectiveBranch,
                effectiveCommitId,
                effectiveWorkflowRunId
            );

            return ResponseEntity.ok(scanExecution);

        } catch (SecurityReportParseException e) {
            log.error("Failed to parse security report from GitHub Actions: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Report Parse Error",
                "message", "Failed to parse security report: " + e.getMessage()
            ));
        } catch (IOException e) {
            log.error("Failed to read uploaded report file: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "File Read Error",
                "message", "Failed to read uploaded file: " + e.getMessage()
            ));
        } catch (DatabaseException e) {
            log.error("Database error during GitHub Actions scan ingestion: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Database error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Unexpected error during GitHub Actions scan ingestion: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Internal Server Error",
                "message", e.getMessage() != null ? e.getMessage() : "Unexpected error"
            ));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "message", "GitHub Actions integration is ready",
            "tokenConfigured", scanIngestionToken != null && !scanIngestionToken.isBlank()
        ));
    }
}