package com.securityintel.deduplication;

import com.securityintel.model.SecurityFinding;
import com.securityintel.repository.SecurityFindingRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DeduplicationEngine {

    private final SecurityFindingRepository securityFindingRepository;

    public DeduplicationEngine(SecurityFindingRepository securityFindingRepository) {
        this.securityFindingRepository = securityFindingRepository;
    }

    /**
     * Processes findings for deduplication and correlation
     * @param newFindings List of new findings to process
     * @return DeduplicationResult containing unique findings and correlation info
     */
    public DeduplicationResult processFindings(List<SecurityFinding> newFindings) {
        List<SecurityFinding> uniqueFindings = new ArrayList<>();
        List<SecurityFinding> duplicateFindings = new ArrayList<>();
        Map<String, List<SecurityFinding>> correlationGroups = new HashMap<>();

        for (SecurityFinding newFinding : newFindings) {
            DeduplicationMatch match = findDuplicates(newFinding);
            
            if (match.hasExactMatch()) {
                // Found exact duplicate - update existing finding
                SecurityFinding existingFinding = match.getExactMatch();
                SecurityFinding updatedFinding = correlateFinding(existingFinding, newFinding);
                uniqueFindings.add(updatedFinding);
                duplicateFindings.add(newFinding);
                
                // Track correlation
                String correlationKey = existingFinding.getFingerprint();
                correlationGroups.computeIfAbsent(correlationKey, k -> new ArrayList<>()).add(newFinding);
                
            } else if (match.hasSimilarMatches()) {
                // Found potential correlations but no exact match
                SecurityFinding correlatedFinding = createCorrelatedFinding(newFinding, match.getSimilarMatches());
                uniqueFindings.add(correlatedFinding);
                
                // Track correlation
                String correlationKey = correlatedFinding.getFingerprint();
                correlationGroups.computeIfAbsent(correlationKey, k -> new ArrayList<>())
                                .addAll(match.getSimilarMatches());
                
            } else {
                // No duplicates found - it's unique
                uniqueFindings.add(newFinding);
            }
        }

        return new DeduplicationResult(uniqueFindings, duplicateFindings, correlationGroups, newFindings.size());
    }

    private DeduplicationMatch findDuplicates(SecurityFinding newFinding) {
        DeduplicationMatch match = new DeduplicationMatch();

        // First try exact fingerprint match
        Optional<SecurityFinding> exactMatch = securityFindingRepository
            .findByFingerprint(newFinding.getFingerprint());
        
        if (exactMatch.isPresent()) {
            match.setExactMatch(exactMatch.get());
            return match;
        }

        // Look for potential correlations using different strategies
        List<SecurityFinding> similarFindings = new ArrayList<>();

        // Strategy 1: CVE-based correlation across different tools
        if (newFinding.getCve() != null && !newFinding.getCve().isEmpty()) {
            List<SecurityFinding> cveMatches = securityFindingRepository
                .findPotentialDuplicatesByCve(
                    newFinding.getCve(),
                    newFinding.getServiceName(),
                    newFinding.getPackageName(),
                    newFinding.getInstalledVersion()
                );
            similarFindings.addAll(cveMatches);
        }

        // Strategy 2: Signature-based correlation (same vulnerability in same location)
        if (similarFindings.isEmpty() && newFinding.getTitle() != null) {
            List<SecurityFinding> signatureMatches = securityFindingRepository
                .findPotentialDuplicatesBySignature(
                    newFinding.getTool(),
                    newFinding.getServiceName(),
                    newFinding.getTitle(),
                    newFinding.getPackageName(),
                    newFinding.getFilePath(),
                    newFinding.getLineNumber()
                );
            similarFindings.addAll(signatureMatches);
        }

        // Filter out findings that are too different
        List<SecurityFinding> validSimilarFindings = similarFindings.stream()
            .filter(finding -> isValidCorrelation(newFinding, finding))
            .collect(Collectors.toList());

        if (!validSimilarFindings.isEmpty()) {
            match.setSimilarMatches(validSimilarFindings);
        }

        return match;
    }

    private boolean isValidCorrelation(SecurityFinding newFinding, SecurityFinding existingFinding) {
        // Must be same service
        if (!Objects.equals(newFinding.getServiceName(), existingFinding.getServiceName())) {
            return false;
        }

        // If both have CVEs, they must match
        if (newFinding.getCve() != null && existingFinding.getCve() != null) {
            return newFinding.getCve().equals(existingFinding.getCve());
        }

        // If both have package names, they must match
        if (newFinding.getPackageName() != null && existingFinding.getPackageName() != null) {
            return newFinding.getPackageName().equals(existingFinding.getPackageName());
        }

        // If both have file paths, they must match
        if (newFinding.getFilePath() != null && existingFinding.getFilePath() != null) {
            return newFinding.getFilePath().equals(existingFinding.getFilePath());
        }

        // Default: allow correlation if we got this far
        return true;
    }

    private SecurityFinding correlateFinding(SecurityFinding existingFinding, SecurityFinding newFinding) {
        // Update existing finding with correlation information
        existingFinding.setUpdatedAt(LocalDateTime.now());

        // Add source finding reference
        List<String> sourceFindings = existingFinding.getSourceFindings();
        if (sourceFindings == null) {
            sourceFindings = new ArrayList<>();
        }
        
        // Add the new finding's tool to source findings
        String sourceReference = newFinding.getTool().name() + ":" + newFinding.getReportId();
        if (!sourceFindings.contains(sourceReference)) {
            sourceFindings.add(sourceReference);
            existingFinding.setSourceFindings(sourceFindings);
        }

        // Update fields with better information if available
        if (existingFinding.getCvssScore() == null && newFinding.getCvssScore() != null) {
            existingFinding.setCvssScore(newFinding.getCvssScore());
        }

        if (existingFinding.getFixedVersion() == null && newFinding.getFixedVersion() != null) {
            existingFinding.setFixedVersion(newFinding.getFixedVersion());
        }

        if (existingFinding.getDescription() == null && newFinding.getDescription() != null) {
            existingFinding.setDescription(newFinding.getDescription());
        }

        // Use higher severity if different
        if (newFinding.getSeverity() != null && 
            (existingFinding.getSeverity() == null || 
             isSeverityHigher(newFinding.getSeverity(), existingFinding.getSeverity()))) {
            existingFinding.setSeverity(newFinding.getSeverity());
        }

        return existingFinding;
    }

    private SecurityFinding createCorrelatedFinding(SecurityFinding newFinding, List<SecurityFinding> similarFindings) {
        // Create source findings list from similar findings
        List<String> sourceFindings = similarFindings.stream()
            .map(sf -> sf.getTool().name() + ":" + sf.getReportId())
            .distinct()
            .collect(Collectors.toList());

        // Add current tool
        sourceFindings.add(newFinding.getTool().name() + ":" + newFinding.getReportId());
        newFinding.setSourceFindings(sourceFindings);

        return newFinding;
    }

    private boolean isSeverityHigher(com.securityintel.model.Severity newSev, com.securityintel.model.Severity existingSev) {
        Map<com.securityintel.model.Severity, Integer> severityRank = Map.of(
            com.securityintel.model.Severity.CRITICAL, 4,
            com.securityintel.model.Severity.HIGH, 3,
            com.securityintel.model.Severity.MEDIUM, 2,
            com.securityintel.model.Severity.LOW, 1,
            com.securityintel.model.Severity.UNKNOWN, 0
        );

        return severityRank.get(newSev) > severityRank.get(existingSev);
    }

    /**
     * Inner class to hold deduplication matching results
     */
    private static class DeduplicationMatch {
        private SecurityFinding exactMatch;
        private List<SecurityFinding> similarMatches;

        public boolean hasExactMatch() {
            return exactMatch != null;
        }

        public boolean hasSimilarMatches() {
            return similarMatches != null && !similarMatches.isEmpty();
        }

        public SecurityFinding getExactMatch() {
            return exactMatch;
        }

        public void setExactMatch(SecurityFinding exactMatch) {
            this.exactMatch = exactMatch;
        }

        public List<SecurityFinding> getSimilarMatches() {
            return similarMatches;
        }

        public void setSimilarMatches(List<SecurityFinding> similarMatches) {
            this.similarMatches = similarMatches;
        }
    }
}