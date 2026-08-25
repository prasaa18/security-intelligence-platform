package com.securityintel.parser;

import com.securityintel.model.Tool;
import com.securityintel.model.ScanType;
import com.securityintel.model.Environment;

import java.util.List;

public class ParsedSecurityReport {
    private Tool tool;
    private ScanType scanType;
    private String serviceName;
    private String repository;
    private String branch;
    private String commitId;
    private Environment environment;
    private List<ParsedFinding> findings;
    private String containerImage;

    public ParsedSecurityReport() {}

    public ParsedSecurityReport(Tool tool, ScanType scanType, String serviceName, 
                              Environment environment, List<ParsedFinding> findings) {
        this.tool = tool;
        this.scanType = scanType;
        this.serviceName = serviceName;
        this.environment = environment;
        this.findings = findings;
    }

    // Getters and Setters
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

    public List<ParsedFinding> getFindings() {
        return findings;
    }

    public void setFindings(List<ParsedFinding> findings) {
        this.findings = findings;
    }

    public String getContainerImage() {
        return containerImage;
    }

    public void setContainerImage(String containerImage) {
        this.containerImage = containerImage;
    }
}