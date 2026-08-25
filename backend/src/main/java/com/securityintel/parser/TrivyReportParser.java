package com.securityintel.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.securityintel.model.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TrivyReportParser implements SecurityReportParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(String content) {
        try {
            JsonNode root = objectMapper.readTree(content);
            // Check for Trivy-specific structure
            return root.has("Results") || root.has("SchemaVersion") || 
                   (root.has("ArtifactName") && root.has("ArtifactType"));
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
            report.setTool(Tool.TRIVY);
            report.setScanType(determineScanType(root));
            report.setServiceName(serviceName);
            report.setEnvironment(environment);
            
            // Extract metadata if available
            if (root.has("ArtifactName")) {
                String artifactName = root.get("ArtifactName").asText();
                if (artifactName.contains("/")) {
                    report.setContainerImage(artifactName);
                    report.setScanType(ScanType.CONTAINER);
                } else {
                    report.setRepository(artifactName);
                }
            }

            List<ParsedFinding> findings = new ArrayList<>();
            
            if (root.has("Results") && root.get("Results").isArray()) {
                for (JsonNode result : root.get("Results")) {
                    findings.addAll(parseResultNode(result));
                }
            }
            
            report.setFindings(findings);
            return report;
            
        } catch (Exception e) {
            throw new SecurityReportParseException("Failed to parse Trivy report: " + e.getMessage(), e);
        }
    }

    @Override
    public String getToolName() {
        return "TRIVY";
    }

    private ScanType determineScanType(JsonNode root) {
        if (root.has("ArtifactType")) {
            String artifactType = root.get("ArtifactType").asText().toLowerCase();
            if (artifactType.contains("container") || artifactType.contains("image")) {
                return ScanType.CONTAINER;
            }
            if (artifactType.contains("filesystem") || artifactType.contains("repository")) {
                return ScanType.SCA; // Software Composition Analysis
            }
        }
        return ScanType.SCA; // Default for Trivy
    }

    private List<ParsedFinding> parseResultNode(JsonNode result) {
        List<ParsedFinding> findings = new ArrayList<>();
        
        String target = result.has("Target") ? result.get("Target").asText() : null;
        
        if (result.has("Vulnerabilities") && result.get("Vulnerabilities").isArray()) {
            for (JsonNode vuln : result.get("Vulnerabilities")) {
                ParsedFinding finding = parseVulnerability(vuln, target);
                if (finding != null) {
                    findings.add(finding);
                }
            }
        }
        
        return findings;
    }

    private ParsedFinding parseVulnerability(JsonNode vuln, String target) {
        ParsedFinding finding = new ParsedFinding();
        
        // CVE ID
        if (vuln.has("VulnerabilityID")) {
            finding.setCve(vuln.get("VulnerabilityID").asText());
        }
        
        // Package information
        if (vuln.has("PkgName")) {
            finding.setPackageName(vuln.get("PkgName").asText());
        }
        
        if (vuln.has("InstalledVersion")) {
            finding.setInstalledVersion(vuln.get("InstalledVersion").asText());
        }
        
        if (vuln.has("FixedVersion")) {
            String fixedVersion = vuln.get("FixedVersion").asText();
            if (fixedVersion != null && !fixedVersion.isEmpty() && !fixedVersion.equals("null")) {
                finding.setFixedVersion(fixedVersion);
            }
        }
        
        // Severity
        if (vuln.has("Severity")) {
            String severity = vuln.get("Severity").asText().toUpperCase();
            finding.setSeverity(mapSeverity(severity));
        }
        
        // CVSS Score
        if (vuln.has("CVSS")) {
            JsonNode cvss = vuln.get("CVSS");
            if (cvss.isObject()) {
                // Try different CVSS versions
                for (String version : new String[]{"nvd", "redhat", "ghsa"}) {
                    if (cvss.has(version) && cvss.get(version).has("V3Score")) {
                        finding.setCvssScore(cvss.get(version).get("V3Score").asDouble());
                        break;
                    }
                }
            }
        }
        
        // Title and Description
        if (vuln.has("Title")) {
            finding.setTitle(vuln.get("Title").asText());
        }
        
        if (vuln.has("Description")) {
            finding.setDescription(vuln.get("Description").asText());
        }
        
        // CWE
        if (vuln.has("CweIDs") && vuln.get("CweIDs").isArray() && vuln.get("CweIDs").size() > 0) {
            finding.setCwe(vuln.get("CweIDs").get(0).asText());
        }
        
        // File path (from target)
        if (target != null && !target.isEmpty()) {
            finding.setFilePath(target);
        }
        
        return finding;
    }

    private Severity mapSeverity(String trivySeverity) {
        if (trivySeverity == null || trivySeverity.isEmpty()) {
            return Severity.UNKNOWN;
        }
        
        switch (trivySeverity.toUpperCase()) {
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