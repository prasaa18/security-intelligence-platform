package com.securityintel.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Document(collection = "remediation_items")
public class RemediationItem {
    @Id
    private String id;
    
    @Indexed
    private String findingId;
    
    @Indexed
    private String serviceName;
    
    private String teamName;
    
    @Indexed
    private Priority priority;
    
    private Integer riskScore;
    
    @Indexed
    private Status status;
    
    @Indexed
    private RemediationStatus remediationStatus;
    
    private String recommendedAction;
    private LocalDateTime targetDate;
    
    private LocalDateTime firstDetectedAt;
    private LocalDateTime lastDetectedAt;
    private LocalDateTime latestScanAt;
    private LocalDateTime resolvedAt;
    
    @Indexed
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;

    public RemediationItem() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = Status.OPEN;
        this.remediationStatus = RemediationStatus.NEW;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFindingId() {
        return findingId;
    }

    public void setFindingId(String findingId) {
        this.findingId = findingId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public RemediationStatus getRemediationStatus() {
        return remediationStatus;
    }

    public void setRemediationStatus(RemediationStatus remediationStatus) {
        this.remediationStatus = remediationStatus;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }

    public void setRecommendedAction(String recommendedAction) {
        this.recommendedAction = recommendedAction;
    }

    public LocalDateTime getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDateTime targetDate) {
        this.targetDate = targetDate;
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

    public LocalDateTime getLatestScanAt() {
        return latestScanAt;
    }

    public void setLatestScanAt(LocalDateTime latestScanAt) {
        this.latestScanAt = latestScanAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
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
}