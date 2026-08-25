package com.securityintel.deduplication;

import com.securityintel.model.SecurityFinding;

import java.util.List;
import java.util.Map;

public class DeduplicationResult {
    private final List<SecurityFinding> uniqueFindings;
    private final List<SecurityFinding> duplicateFindings;
    private final Map<String, List<SecurityFinding>> correlationGroups;

    private final int totalRawFindings;

    public DeduplicationResult(List<SecurityFinding> uniqueFindings, 
                             List<SecurityFinding> duplicateFindings,
                             Map<String, List<SecurityFinding>> correlationGroups,
                             int totalRawFindings) {
        this.uniqueFindings = uniqueFindings;
        this.duplicateFindings = duplicateFindings;
        this.correlationGroups = correlationGroups;
        this.totalRawFindings = totalRawFindings;
    }

    public DeduplicationResult(List<SecurityFinding> uniqueFindings, 
                             List<SecurityFinding> duplicateFindings,
                             Map<String, List<SecurityFinding>> correlationGroups) {
        this(uniqueFindings, duplicateFindings, correlationGroups, uniqueFindings.size());
    }

    /**
     * Get findings that are unique (no duplicates found)
     */
    public List<SecurityFinding> getUniqueFindings() {
        return uniqueFindings;
    }

    /**
     * Get findings that were identified as duplicates
     */
    public List<SecurityFinding> getDuplicateFindings() {
        return duplicateFindings;
    }

    /**
     * Get correlation groups - findings that were correlated together
     */
    public Map<String, List<SecurityFinding>> getCorrelationGroups() {
        return correlationGroups;
    }

    /**
     * Get total number of raw findings processed
     */
    public int getTotalRawFindings() {
        return totalRawFindings;
    }

    /**
     * Get number of unique findings after deduplication
     */
    public int getUniqueCount() {
        return uniqueFindings.size();
    }

    /**
     * Get number of duplicate findings removed
     */
    public int getDuplicateCount() {
        return duplicateFindings.size();
    }

    /**
     * Get number of correlation groups formed
     */
    public int getCorrelationGroupCount() {
        return correlationGroups.size();
    }
}