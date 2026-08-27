package com.securityintel.dto;

import com.securityintel.model.*;

import java.time.LocalDateTime;
import java.util.List;

public class SecurityFindingDto {
    private String id;
    private String reportId;
    private Tool tool;
    private ScanType scanType;
    private String serviceName;
    private String repository;
    private Environment environment;
    private Severity severity;
    private Double cvssScore;
    private String cve;
    private String cwe;
    private String title;
    private String description;
    private String packageName;
    private String installedVersion;
    private String fixedVersion;
    private String filePath;
    private Integer lineNumber;
    private String containerImage;
    private String endpoint;
    private String httpMethod;
    private Status status;
    private String fingerprint;
    private List<String> sourceFindings;
    private Double riskScore;
    private Priority priority;
    private List<String> priorityReasons;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime firstDetectedAt;
    private LocalDateTime lastDetectedAt;
    private String latestScanId;
    private DetectionState detectionState;

    // Constructors
    public SecurityFindingDto() {}

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public Tool getTool() {
        return tool;
    }

    public void setTool(Tool tool) {
        this.tool = tool;
    }

    public ScanType getScanType() {
        return scanType;
    }

    public void setScanType(ScanType scanType) {
        this.scanType = scanType;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public Double getCvssScore() {
        return cvssScore;
    }

    public void setCvssScore(Double cvssScore) {
        this.cvssScore = cvssScore;
    }

    public String getCve() {
        return cve;
    }

    public void setCve(String cve) {
        this.cve = cve;
    }

    public String getCwe() {
        return cwe;
    }

    public void setCwe(String cwe) {
        this.cwe = cwe;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getInstalledVersion() {
        return installedVersion;
    }

    public void setInstalledVersion(String installedVersion) {
        this.installedVersion = installedVersion;
    }

    public String getFixedVersion() {
        return fixedVersion;
    }

    public void setFixedVersion(String fixedVersion) {
        this.fixedVersion = fixedVersion;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getContainerImage() {
        return containerImage;
    }

    public void setContainerImage(String containerImage) {
        this.containerImage = containerImage;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public List<String> getSourceFindings() {
        return sourceFindings;
    }

    public void setSourceFindings(List<String> sourceFindings) {
        this.sourceFindings = sourceFindings;
    }

    public Double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Double riskScore) {
        this.riskScore = riskScore;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public List<String> getPriorityReasons() {
        return priorityReasons;
    }

    public void setPriorityReasons(List<String> priorityReasons) {
        this.priorityReasons = priorityReasons;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getFirstDetectedAt() { return firstDetectedAt; }
    public void setFirstDetectedAt(LocalDateTime firstDetectedAt) { this.firstDetectedAt = firstDetectedAt; }
    public LocalDateTime getLastDetectedAt() { return lastDetectedAt; }
    public void setLastDetectedAt(LocalDateTime lastDetectedAt) { this.lastDetectedAt = lastDetectedAt; }
    public String getLatestScanId() { return latestScanId; }
    public void setLatestScanId(String latestScanId) { this.latestScanId = latestScanId; }
    public DetectionState getDetectionState() { return detectionState; }
    public void setDetectionState(DetectionState detectionState) { this.detectionState = detectionState; }
}