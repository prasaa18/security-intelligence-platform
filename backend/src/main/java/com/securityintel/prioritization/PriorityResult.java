package com.securityintel.prioritization;

import com.securityintel.model.Priority;

import java.util.List;

public class PriorityResult {
    private final int riskScore;
    private final Priority priority;
    private final List<String> reasons;

    public PriorityResult(int riskScore, Priority priority, List<String> reasons) {
        this.riskScore = riskScore;
        this.priority = priority;
        this.reasons = reasons;
    }

    /**
     * Get the calculated risk score (0-100)
     */
    public int getRiskScore() {
        return riskScore;
    }

    /**
     * Get the determined priority (P0-P4)
     */
    public Priority getPriority() {
        return priority;
    }

    /**
     * Get human-readable reasons for the priority calculation
     */
    public List<String> getReasons() {
        return reasons;
    }
}