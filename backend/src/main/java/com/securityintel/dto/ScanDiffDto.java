package com.securityintel.dto;

import com.securityintel.model.ScanExecution;
import java.util.List;

public class ScanDiffDto {
    private ScanExecution baselineScan;
    private ScanExecution targetScan;
    private int newCount;
    private int resolvedCount;
    private int persistentCount;
    private List<SecurityFindingDto> newFindings;
    private List<SecurityFindingDto> resolvedFindings;
    private List<SecurityFindingDto> persistentFindings;

    public ScanDiffDto() {
    }

    public ScanDiffDto(ScanExecution baselineScan, ScanExecution targetScan,
                       int newCount, int resolvedCount, int persistentCount,
                       List<SecurityFindingDto> newFindings,
                       List<SecurityFindingDto> resolvedFindings,
                       List<SecurityFindingDto> persistentFindings) {
        this.baselineScan = baselineScan;
        this.targetScan = targetScan;
        this.newCount = newCount;
        this.resolvedCount = resolvedCount;
        this.persistentCount = persistentCount;
        this.newFindings = newFindings;
        this.resolvedFindings = resolvedFindings;
        this.persistentFindings = persistentFindings;
    }

    public ScanExecution getBaselineScan() {
        return baselineScan;
    }

    public void setBaselineScan(ScanExecution baselineScan) {
        this.baselineScan = baselineScan;
    }

    public ScanExecution getTargetScan() {
        return targetScan;
    }

    public void setTargetScan(ScanExecution targetScan) {
        this.targetScan = targetScan;
    }

    public int getNewCount() {
        return newCount;
    }

    public void setNewCount(int newCount) {
        this.newCount = newCount;
    }

    public int getResolvedCount() {
        return resolvedCount;
    }

    public void setResolvedCount(int resolvedCount) {
        this.resolvedCount = resolvedCount;
    }

    public int getPersistentCount() {
        return persistentCount;
    }

    public void setPersistentCount(int persistentCount) {
        this.persistentCount = persistentCount;
    }

    public List<SecurityFindingDto> getNewFindings() {
        return newFindings;
    }

    public void setNewFindings(List<SecurityFindingDto> newFindings) {
        this.newFindings = newFindings;
    }

    public List<SecurityFindingDto> getResolvedFindings() {
        return resolvedFindings;
    }

    public void setResolvedFindings(List<SecurityFindingDto> resolvedFindings) {
        this.resolvedFindings = resolvedFindings;
    }

    public List<SecurityFindingDto> getPersistentFindings() {
        return persistentFindings;
    }

    public void setPersistentFindings(List<SecurityFindingDto> persistentFindings) {
        this.persistentFindings = persistentFindings;
    }
}