package com.securityintel.normalization;

import com.securityintel.model.SecurityFinding;
import com.securityintel.parser.ParsedFinding;
import com.securityintel.parser.ParsedSecurityReport;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class FindingNormalizer {

    /**
     * Normalizes parsed findings into SecurityFinding entities
     * @param parsedReport The parsed security report
     * @param reportId The ID of the scan report
     * @return List of normalized SecurityFinding entities
     */
    public List<SecurityFinding> normalize(ParsedSecurityReport parsedReport, String reportId) {
        List<SecurityFinding> normalizedFindings = new ArrayList<>();
        
        for (ParsedFinding parsedFinding : parsedReport.getFindings()) {
            SecurityFinding finding = normalizeFinding(parsedFinding, parsedReport, reportId);
            normalizedFindings.add(finding);
        }
        
        return normalizedFindings;
    }

    private SecurityFinding normalizeFinding(ParsedFinding parsedFinding, 
                                           ParsedSecurityReport parsedReport, 
                                           String reportId) {
        SecurityFinding finding = new SecurityFinding();
        
        // Basic metadata
        finding.setReportId(reportId);
        finding.setTool(parsedReport.getTool());
        finding.setScanType(parsedReport.getScanType());
        finding.setServiceName(parsedReport.getServiceName());
        finding.setRepository(parsedReport.getRepository());
        finding.setEnvironment(parsedReport.getEnvironment());
        
        // Vulnerability details from parsed finding
        finding.setSeverity(parsedFinding.getSeverity());
        finding.setCvssScore(parsedFinding.getCvssScore());
        finding.setCve(cleanCveId(parsedFinding.getCve()));
        finding.setCwe(cleanCweId(parsedFinding.getCwe()));
        finding.setTitle(cleanTitle(parsedFinding.getTitle()));
        finding.setDescription(cleanDescription(parsedFinding.getDescription()));
        
        // Package information
        finding.setPackageName(cleanPackageName(parsedFinding.getPackageName()));
        finding.setInstalledVersion(cleanVersion(parsedFinding.getInstalledVersion()));
        finding.setFixedVersion(cleanVersion(parsedFinding.getFixedVersion()));
        
        // Location information
        finding.setFilePath(cleanFilePath(parsedFinding.getFilePath()));
        finding.setLineNumber(parsedFinding.getLineNumber());
        finding.setContainerImage(cleanContainerImage(parsedFinding.getContainerImage(), parsedReport.getContainerImage()));
        
        // API/Web-specific
        finding.setEndpoint(parsedFinding.getEndpoint());
        finding.setHttpMethod(parsedFinding.getHttpMethod());
        
        // Generate fingerprint for deduplication
        finding.setFingerprint(generateFingerprint(finding));
        
        // Set timestamps
        finding.setCreatedAt(LocalDateTime.now());
        finding.setUpdatedAt(LocalDateTime.now());
        
        return finding;
    }

    private String cleanCveId(String cve) {
        if (cve == null || cve.trim().isEmpty()) {
            return null;
        }
        
        String cleaned = cve.trim().toUpperCase();
        
        // Ensure CVE format: CVE-YYYY-NNNN
        if (!cleaned.startsWith("CVE-")) {
            return null;
        }
        
        return cleaned;
    }

    private String cleanCweId(String cwe) {
        if (cwe == null || cwe.trim().isEmpty()) {
            return null;
        }
        
        String cleaned = cwe.trim().toUpperCase();
        
        // Ensure CWE format: CWE-NNN
        if (!cleaned.startsWith("CWE-")) {
            // Try to extract number if it's just a number
            if (cleaned.matches("\\d+")) {
                return "CWE-" + cleaned;
            }
            return null;
        }
        
        return cleaned;
    }

    private String cleanTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return null;
        }
        
        // Truncate if too long, clean up whitespace
        String cleaned = title.trim().replaceAll("\\s+", " ");
        if (cleaned.length() > 500) {
            cleaned = cleaned.substring(0, 497) + "...";
        }
        
        return cleaned;
    }

    private String cleanDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return null;
        }
        
        // Clean up whitespace and truncate if necessary
        String cleaned = description.trim().replaceAll("\\s+", " ");
        if (cleaned.length() > 2000) {
            cleaned = cleaned.substring(0, 1997) + "...";
        }
        
        return cleaned;
    }

    private String cleanPackageName(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) {
            return null;
        }
        
        return packageName.trim().toLowerCase();
    }

    private String cleanVersion(String version) {
        if (version == null || version.trim().isEmpty()) {
            return null;
        }
        
        String cleaned = version.trim();
        
        // Remove common prefixes/suffixes that don't belong in versions
        cleaned = cleaned.replaceAll("^v", "");  // Remove leading 'v'
        cleaned = cleaned.replaceAll("\\s+", ""); // Remove spaces
        
        if (cleaned.isEmpty() || cleaned.equals("null") || cleaned.equals("undefined")) {
            return null;
        }
        
        return cleaned;
    }

    private String cleanFilePath(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return null;
        }
        
        // Normalize path separators and clean up
        String cleaned = filePath.trim().replace("\\", "/");
        
        // Remove leading ./ if present
        if (cleaned.startsWith("./")) {
            cleaned = cleaned.substring(2);
        }
        
        return cleaned;
    }

    private String cleanContainerImage(String findingImage, String reportImage) {
        // Use finding-specific image if available, otherwise fall back to report image
        String image = findingImage != null ? findingImage : reportImage;
        
        if (image == null || image.trim().isEmpty()) {
            return null;
        }
        
        return image.trim().toLowerCase();
    }

    /**
     * Generates a deterministic fingerprint for deduplication
     * Primary: CVE + serviceName + packageName + installedVersion
     * Fallback: tool + serviceName + title + packageName + filePath + lineNumber
     */
    private String generateFingerprint(SecurityFinding finding) {
        StringBuilder fingerprint = new StringBuilder();
        
        // Primary fingerprint using CVE
        if (finding.getCve() != null && !finding.getCve().isEmpty()) {
            fingerprint.append("CVE:")
                      .append(finding.getCve())
                      .append("|SVC:")
                      .append(orEmpty(finding.getServiceName()))
                      .append("|PKG:")
                      .append(orEmpty(finding.getPackageName()))
                      .append("|VER:")
                      .append(orEmpty(finding.getInstalledVersion()));
        } else {
            // Fallback fingerprint without CVE
            fingerprint.append("TOOL:")
                      .append(finding.getTool().name())
                      .append("|SVC:")
                      .append(orEmpty(finding.getServiceName()))
                      .append("|TITLE:")
                      .append(orEmpty(finding.getTitle()))
                      .append("|PKG:")
                      .append(orEmpty(finding.getPackageName()))
                      .append("|PATH:")
                      .append(orEmpty(finding.getFilePath()))
                      .append("|LINE:")
                      .append(finding.getLineNumber() != null ? finding.getLineNumber().toString() : "");
        }
        
        return fingerprint.toString();
    }

    private String orEmpty(String value) {
        return value != null ? value : "";
    }
}