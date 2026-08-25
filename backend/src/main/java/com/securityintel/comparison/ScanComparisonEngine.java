package com.securityintel.comparison;

import com.securityintel.model.DetectionState;
import com.securityintel.model.ScanExecution;
import com.securityintel.model.SecurityFinding;
import com.securityintel.repository.ScanExecutionRepository;
import com.securityintel.repository.SecurityFindingRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ScanComparisonEngine {

    private final SecurityFindingRepository securityFindingRepository;
    private final ScanExecutionRepository scanExecutionRepository;

    public ScanComparisonEngine(SecurityFindingRepository securityFindingRepository,
                               ScanExecutionRepository scanExecutionRepository) {
        this.securityFindingRepository = securityFindingRepository;
        this.scanExecutionRepository = scanExecutionRepository;
    }

    public ScanComparisonResult compareWithPreviousScan(
            List<SecurityFinding> currentFindings,
            ScanExecution currentScanExecution) {
        
        String serviceName = currentScanExecution.getServiceName();
        String tool = currentScanExecution.getTool().name();
        String scanType = currentScanExecution.getScanType().name();
        
        // Find previous successful scan for the same service, tool, and scan type
        var previousScanOpt = scanExecutionRepository
            .findFirstByServiceNameAndToolAndScanTypeAndStatusOrderByCreatedAtDesc(
                serviceName, 
                currentScanExecution.getTool(), 
                currentScanExecution.getScanType(),
                com.securityintel.model.Status.SUCCESS);
        
        List<SecurityFinding> previousFindings = previousScanOpt
            .map(scan -> securityFindingRepository.findByLatestScanId(scan.getId()))
            .orElse(new ArrayList<>());
        
        // Create fingerprint maps for efficient comparison
        Map<String, SecurityFinding> currentFingerprints = currentFindings.stream()
            .collect(Collectors.toMap(SecurityFinding::getFingerprint, f -> f, (a, b) -> a));
        
        Map<String, SecurityFinding> previousFingerprints = previousFindings.stream()
            .collect(Collectors.toMap(SecurityFinding::getFingerprint, f -> f, (a, b) -> a));
        
        List<SecurityFinding> newFindings = new ArrayList<>();
        List<SecurityFinding> unchangedFindings = new ArrayList<>();
        List<SecurityFinding> resolvedFindings = new ArrayList<>();
        
        // Classify current findings
        for (SecurityFinding currentFinding : currentFindings) {
            String fingerprint = currentFinding.getFingerprint();
            
            if (previousFingerprints.containsKey(fingerprint)) {
                // Finding existed in previous scan - UNCHANGED
                currentFinding.setDetectionState(DetectionState.PRESENT);
                currentFinding.setFirstDetectedAt(previousFingerprints.get(fingerprint).getFirstDetectedAt());
                currentFinding.setLastDetectedAt(LocalDateTime.now());
                unchangedFindings.add(currentFinding);
            } else {
                // Finding did not exist in previous scan - NEW
                currentFinding.setDetectionState(DetectionState.NEW);
                currentFinding.setFirstDetectedAt(LocalDateTime.now());
                currentFinding.setLastDetectedAt(LocalDateTime.now());
                newFindings.add(currentFinding);
            }
            
            // Update latest scan reference
            currentFinding.setLatestScanId(currentScanExecution.getId());
        }
        
        // Find resolved findings (existed in previous but not in current)
        for (SecurityFinding previousFinding : previousFindings) {
            String fingerprint = previousFinding.getFingerprint();
            
            if (!currentFingerprints.containsKey(fingerprint)) {
                // Finding existed before but not in current scan - NOT_DETECTED_IN_LATEST_SCAN
                previousFinding.setDetectionState(DetectionState.NOT_DETECTED_IN_LATEST_SCAN);
                previousFinding.setLastDetectedAt(LocalDateTime.now());
                resolvedFindings.add(previousFinding);
            }
        }
        
        return new ScanComparisonResult(
            newFindings,
            unchangedFindings,
            resolvedFindings,
            previousFindings.size()
        );
    }

    public static class ScanComparisonResult {
        private final List<SecurityFinding> newFindings;
        private final List<SecurityFinding> unchangedFindings;
        private final List<SecurityFinding> resolvedFindings;
        private final int previousFindingsCount;

        public ScanComparisonResult(List<SecurityFinding> newFindings,
                                   List<SecurityFinding> unchangedFindings,
                                   List<SecurityFinding> resolvedFindings,
                                   int previousFindingsCount) {
            this.newFindings = newFindings;
            this.unchangedFindings = unchangedFindings;
            this.resolvedFindings = resolvedFindings;
            this.previousFindingsCount = previousFindingsCount;
        }

        public List<SecurityFinding> getNewFindings() {
            return newFindings;
        }

        public List<SecurityFinding> getUnchangedFindings() {
            return unchangedFindings;
        }

        public List<SecurityFinding> getResolvedFindings() {
            return resolvedFindings;
        }

        public int getPreviousFindingsCount() {
            return previousFindingsCount;
        }
    }
}