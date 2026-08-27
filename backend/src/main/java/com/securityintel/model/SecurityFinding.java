package com.securityintel.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "security_findings")
public class SecurityFinding {
    @Id
    private String id;
    
    @Indexed
    private String reportId;
    
    @Indexed
    private Tool tool;
    
    @Indexed
    private ScanType scanType;
    
    @Indexed
    private String serviceName;
    
    private String repository;
    
    @Indexed
    private Environment environment;
    
    @Indexed
    private Severity severity;
    
    private Double cvssScore;
    
    @Indexed
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
    
    @Indexed
    private Status status;
    
    @Indexed
    private String fingerprint;
    
    // For deduplication - store IDs of source findings
    private List<String> sourceFindings;
    
    // Prioritization fields
    private Double riskScore;
    private Priority priority;
    private List<String> priorityReasons;
    
    @Indexed
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    // Detection tracking
    private LocalDateTime firstDetectedAt;
    private LocalDateTime lastDetectedAt;
    private String latestScanId;
    private String scanExecutionId;
    private DetectionState detectionState;

    public SecurityFinding() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = Status.OPEN;
    }

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

    public LocalDateTime getFirstDetectedAt() {
        return firstDetectedAt;
    }

    public void setFirstDetectedAt(LocalDateTime firstDetectedAt) {
        this.firstDetectedAt = firstDetectedAt;
    }

    public LocalDateTime getLastDetectedAt() {
        return lastDetectedAt;
    }

    public void setLastDetectedAt(LocalDateTime lastDetectedAt) {
        this.lastDetectedAt = lastDetectedAt;
    }

    public String getLatestScanId() {
        return latestScanId;
    }

    public void setLatestScanId(String latestScanId) {
        this.latestScanId = latestScanId;
    }

    public String getScanExecutionId() {
        return scanExecutionId;
    }

    public void setScanExecutionId(String scanExecutionId) {
        this.scanExecutionId = scanExecutionId;
    }

    public DetectionState getDetectionState() {
        return detectionState;
    }

    public void setDetectionState(DetectionState detectionState) {
        this.detectionState = detectionState;
    }
}