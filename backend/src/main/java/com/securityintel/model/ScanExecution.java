package com.securityintel.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Document(collection = "scan_executions")
public class ScanExecution {
    @Id
    private String id;
    
    @Indexed
    private String serviceName;
    
    private String repository;
    private String branch;
    private String commitId;
    
    @Indexed
    private Tool tool;
    
    @Indexed
    private ScanType scanType;
    
    @Indexed
    private Environment environment;
    
    @Indexed
    private Status status;
    
    @Indexed
    private TriggerType triggerType;
    
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime receivedAt;
    
    private int totalRawFindings;
    private int totalUniqueFindings;
    private int criticalCount;
    private int highCount;
    private int mediumCount;
    private int lowCount;
    private int newFindings;
    private int resolvedFindings;
    private int unchangedFindings;
    
    @Indexed
    private LocalDateTime createdAt;

    public ScanExecution() {
        this.createdAt = LocalDateTime.now();
        this.receivedAt = LocalDateTime.now();
        this.status = Status.RECEIVED;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
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

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public TriggerType getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(TriggerType triggerType) {
        this.triggerType = triggerType;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    public int getTotalRawFindings() {
        return totalRawFindings;
    }

    public void setTotalRawFindings(int totalRawFindings) {
        this.totalRawFindings = totalRawFindings;
    }

    public int getTotalUniqueFindings() {
        return totalUniqueFindings;
    }

    public void setTotalUniqueFindings(int totalUniqueFindings) {
        this.totalUniqueFindings = totalUniqueFindings;
    }

    public int getCriticalCount() {
        return criticalCount;
    }

    public void setCriticalCount(int criticalCount) {
        this.criticalCount = criticalCount;
    }

    public int getHighCount() {
        return highCount;
    }

    public void setHighCount(int highCount) {
        this.highCount = highCount;
    }

    public int getMediumCount() {
        return mediumCount;
    }

    public void setMediumCount(int mediumCount) {
        this.mediumCount = mediumCount;
    }

    public int getLowCount() {
        return lowCount;
    }

    public void setLowCount(int lowCount) {
        this.lowCount = lowCount;
    }

    public int getNewFindings() {
        return newFindings;
    }

    public void setNewFindings(int newFindings) {
        this.newFindings = newFindings;
    }

    public int getResolvedFindings() {
        return resolvedFindings;
    }

    public void setResolvedFindings(int resolvedFindings) {
        this.resolvedFindings = resolvedFindings;
    }

    public int getUnchangedFindings() {
        return unchangedFindings;
    }

    public void setUnchangedFindings(int unchangedFindings) {
        this.unchangedFindings = unchangedFindings;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}