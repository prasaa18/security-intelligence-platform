package com.securityintel.dto;

import com.securityintel.model.Tool;
import com.securityintel.model.ScanType;

import java.util.List;
import java.util.Map;

public class DashboardSummaryDto {
    private long totalFindings;
    private long uniqueFindings;
    private long critical;
    private long high;
    private long medium;
    private long low;
    private long p0;
    private long p1;
    private long p2;
    private long p3;
    private long p4;
    private List<TopPriorityFindingDto> topPriorities;
    private Map<Tool, Long> scannerDistribution;
    private Map<ScanType, Long> scanTypeDistribution;

    // Constructors
    public DashboardSummaryDto() {}

    // Getters and Setters
    public long getTotalFindings() {
        return totalFindings;
    }

    public void setTotalFindings(long totalFindings) {
        this.totalFindings = totalFindings;
    }

    public long getUniqueFindings() {
        return uniqueFindings;
    }

    public void setUniqueFindings(long uniqueFindings) {
        this.uniqueFindings = uniqueFindings;
    }

    public long getCritical() {
        return critical;
    }

    public void setCritical(long critical) {
        this.critical = critical;
    }

    public long getHigh() {
        return high;
    }

    public void setHigh(long high) {
        this.high = high;
    }

    public long getMedium() {
        return medium;
    }

    public void setMedium(long medium) {
        this.medium = medium;
    }

    public long getLow() {
        return low;
    }

    public void setLow(long low) {
        this.low = low;
    }

    public long getP0() {
        return p0;
    }

    public void setP0(long p0) {
        this.p0 = p0;
    }

    public long getP1() {
        return p1;
    }

    public void setP1(long p1) {
        this.p1 = p1;
    }

    public long getP2() {
        return p2;
    }

    public void setP2(long p2) {
        this.p2 = p2;
    }

    public long getP3() {
        return p3;
    }

    public void setP3(long p3) {
        this.p3 = p3;
    }

    public long getP4() {
        return p4;
    }

    public void setP4(long p4) {
        this.p4 = p4;
    }

    public List<TopPriorityFindingDto> getTopPriorities() {
        return topPriorities;
    }

    public void setTopPriorities(List<TopPriorityFindingDto> topPriorities) {
        this.topPriorities = topPriorities;
    }

    public Map<Tool, Long> getScannerDistribution() {
        return scannerDistribution;
    }

    public void setScannerDistribution(Map<Tool, Long> scannerDistribution) {
        this.scannerDistribution = scannerDistribution;
    }

    public Map<ScanType, Long> getScanTypeDistribution() {
        return scanTypeDistribution;
    }

    public void setScanTypeDistribution(Map<ScanType, Long> scanTypeDistribution) {
        this.scanTypeDistribution = scanTypeDistribution;
    }

    // Inner class for top priority findings
    public static class TopPriorityFindingDto {
        private String cve;
        private String serviceName;
        private String severity;
        private Double riskScore;
        private String priority;

        public TopPriorityFindingDto() {}

        public TopPriorityFindingDto(String cve, String serviceName, String severity, 
                                   Integer riskScore, String priority) {
            this.cve = cve;
            this.serviceName = serviceName;
            this.severity = severity;
            this.riskScore = riskScore;
            this.priority = priority;
        }

        // Getters and Setters
        public String getCve() {
            return cve;
        }

        public void setCve(String cve) {
            this.cve = cve;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getSeverity() {
            return severity;
        }

        public void setSeverity(String severity) {
            this.severity = severity;
        }

        public Double getRiskScore() {
            return riskScore;
        }

        public void setRiskScore(Double riskScore) {
            this.riskScore = riskScore;
        }

        public String getPriority() {
            return priority;
        }

        public void setPriority(String priority) {
            this.priority = priority;
        }
    }
}