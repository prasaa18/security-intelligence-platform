package com.securityintel.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.securityintel.model.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SnykReportParser implements SecurityReportParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(String content) {
        try {
            JsonNode root = objectMapper.readTree(content);
            // Check for Snyk-specific structure
            return root.has("vulnerabilities") || root.has("projectName") || 
                   (root.has("summary") && root.get("summary").has("vulnerabilities"));
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public ParsedSecurityReport parse(String content, String serviceName, Environment environment) 
            throws SecurityReportParseException {
        try {
            JsonNode root = objectMapper.readTree(content);
            
            ParsedSecurityReport report = new ParsedSecurityReport();
            report.setTool(Tool.SNYK);
            report.setScanType(determineScanType(root));
            report.setServiceName(serviceName);
            report.setEnvironment(environment);
            
            // Extract project metadata
            if (root.has("projectName")) {
                report.setRepository(root.get("projectName").asText());
            }

            List<ParsedFinding> findings = new ArrayList<>();
            
            if (root.has("vulnerabilities") && root.get("vulnerabilities").isArray()) {
                for (JsonNode vuln : root.get("vulnerabilities")) {
                    ParsedFinding finding = parseVulnerability(vuln);
                    if (finding != null) {
                        findings.add(finding);
                    }
                }
            }
            
            report.setFindings(findings);
            return report;
            
        } catch (Exception e) {
            throw new SecurityReportParseException("Failed to parse Snyk report: " + e.getMessage(), e);
        }
    }

    @Override
    public String getToolName() {
        return "SNYK";
    }

    private ScanType determineScanType(JsonNode root) {
        // Snyk can detect different types, default to SCA
        if (root.has("packageManager")) {
            return ScanType.SCA;
        }
        if (root.has("dockerImageId")) {
            return ScanType.CONTAINER;
        }
        return ScanType.SCA; // Default for Snyk
    }

    private ParsedFinding parseVulnerability(JsonNode vuln) {
        ParsedFinding finding = new ParsedFinding();
        
        // CVE ID
        if (vuln.has("identifiers") && vuln.get("identifiers").has("CVE")) {
            JsonNode cveArray = vuln.get("identifiers").get("CVE");
            if (cveArray.isArray() && cveArray.size() > 0) {
                finding.setCve(cveArray.get(0).asText());
            }
        }
        
        // Alternative CVE location
        if (finding.getCve() == null && vuln.has("CVSSv3")) {
            String title = vuln.has("title") ? vuln.get("title").asText() : "";
            if (title.contains("CVE-")) {
                String[] parts = title.split("CVE-");
                if (parts.length > 1) {
                    String cve = "CVE-" + parts[1].split(" ")[0];
                    finding.setCve(cve);
                }
            }
        }
        
        // Package information
        if (vuln.has("packageName")) {
            finding.setPackageName(vuln.get("packageName").asText());
        }
        
        if (vuln.has("version")) {
            finding.setInstalledVersion(vuln.get("version").asText());
        }
        
        // Fixed version from fixedIn or upgradePath
        if (vuln.has("fixedIn")) {
            JsonNode fixedIn = vuln.get("fixedIn");
            if (fixedIn.isArray() && fixedIn.size() > 0) {
                finding.setFixedVersion(fixedIn.get(0).asText());
            } else if (fixedIn.isTextual()) {
                finding.setFixedVersion(fixedIn.asText());
            }
        }
        if (finding.getFixedVersion() == null && vuln.has("upgradePath") && vuln.get("upgradePath").isArray()) {
            JsonNode upgradePath = vuln.get("upgradePath");
            for (JsonNode upgrade : upgradePath) {
                if (upgrade.asText().contains("@")) {
                    String version = upgrade.asText().split("@")[1];
                    finding.setFixedVersion(version);
                    break;
                }
            }
        }
        
        // Severity
        if (vuln.has("severity")) {
            String severity = vuln.get("severity").asText().toUpperCase();
            finding.setSeverity(mapSeverity(severity));
        }
        
        // CVSS Score
        if (vuln.has("cvssScore") && vuln.get("cvssScore").isNumber()) {
            finding.setCvssScore(vuln.get("cvssScore").asDouble());
        } else if (vuln.has("CVSSv3") && vuln.get("CVSSv3").isNumber()) {
            finding.setCvssScore(vuln.get("CVSSv3").asDouble());
        }
        
        // Title and Description
        if (vuln.has("title")) {
            finding.setTitle(vuln.get("title").asText());
        }
        
        if (vuln.has("description")) {
            finding.setDescription(vuln.get("description").asText());
        }
        
        // CWE
        if (vuln.has("identifiers") && vuln.get("identifiers").has("CWE")) {
            JsonNode cweArray = vuln.get("identifiers").get("CWE");
            if (cweArray.isArray() && cweArray.size() > 0) {
                finding.setCwe(cweArray.get(0).asText());
            }
        }
        
        // File information
        if (vuln.has("from") && vuln.get("from").isArray() && vuln.get("from").size() > 0) {
            finding.setFilePath(vuln.get("from").get(0).asText());
        }
        
        return finding;
    }

    private Severity mapSeverity(String snykSeverity) {
        if (snykSeverity == null || snykSeverity.isEmpty()) {
            return Severity.UNKNOWN;
        }
        
        switch (snykSeverity.toUpperCase()) {
            case "CRITICAL":
                return Severity.CRITICAL;
            case "HIGH":
                return Severity.HIGH;
            case "MEDIUM":
                return Severity.MEDIUM;
            case "LOW":
                return Severity.LOW;
            default:
                return Severity.UNKNOWN;
        }
    }
}