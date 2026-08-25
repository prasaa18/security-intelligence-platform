package com.securityintel.dto;

import com.securityintel.model.*;

import java.time.LocalDateTime;

public class ScanReportDto {
    private String id;
    private Tool tool;
    private ScanType scanType;
    private String serviceName;
    private String repository;
    private String branch;
    private String commitId;
    private Environment environment;
    private String uploadedFileName;
    private int totalFindings;
    private Status status;
    private LocalDateTime createdAt;

    // Constructors
    public ScanReportDto() {}

    public ScanReportDto(Tool tool, ScanType scanType, String serviceName, 
                        Environment environment, String uploadedFileName, int totalFindings) {
        this.tool = tool;
        this.scanType = scanType;
        this.serviceName = serviceName;
        this.environment = environment;
        this.uploadedFileName = uploadedFileName;
        this.totalFindings = totalFindings;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public String getUploadedFileName() {
        return uploadedFileName;
    }

    public void setUploadedFileName(String uploadedFileName) {
        this.uploadedFileName = uploadedFileName;
    }

    public int getTotalFindings() {
        return totalFindings;
    }

    public void setTotalFindings(int totalFindings) {
        this.totalFindings = totalFindings;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}