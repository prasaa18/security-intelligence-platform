package com.securityintel.scan;

import com.securityintel.comparison.ScanComparisonEngine;
import com.securityintel.comparison.ScanComparisonEngine.ScanComparisonResult;
import com.securityintel.deduplication.DeduplicationEngine;
import com.securityintel.deduplication.DeduplicationResult;
import com.securityintel.exception.DatabaseException;
import com.securityintel.model.*;
import com.securityintel.normalization.FindingNormalizer;
import com.securityintel.parser.ParsedSecurityReport;
import com.securityintel.parser.SecurityReportParseException;
import com.securityintel.parser.SecurityReportParser;
import com.securityintel.parser.SecurityReportParserFactory;
import com.securityintel.prioritization.PriorityResult;
import com.securityintel.prioritization.SecurityPrioritizationEngine;
import com.securityintel.remediation.RemediationService;
import com.securityintel.repository.ScanExecutionRepository;
import com.securityintel.repository.SecurityFindingRepository;
import com.securityintel.repository.ServiceRepository;
import com.securityintel.securitystate.SecurityStateCalculator;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class ScanExecutionService {

    private final ScanExecutionRepository scanExecutionRepository;
    private final SecurityFindingRepository securityFindingRepository;
    private final ServiceRepository serviceRepository;
    private final SecurityReportParserFactory parserFactory;
    private final FindingNormalizer findingNormalizer;
    private final DeduplicationEngine deduplicationEngine;
    private final SecurityPrioritizationEngine prioritizationEngine;
    private final ScanComparisonEngine scanComparisonEngine;
    private final RemediationService remediationService;
    private final SecurityStateCalculator securityStateCalculator;

    public ScanExecutionService(ScanExecutionRepository scanExecutionRepository,
                               SecurityFindingRepository securityFindingRepository,
                               ServiceRepository serviceRepository,
                               SecurityReportParserFactory parserFactory,
                               FindingNormalizer findingNormalizer,
                               DeduplicationEngine deduplicationEngine,
                               SecurityPrioritizationEngine prioritizationEngine,
                               ScanComparisonEngine scanComparisonEngine,
                               RemediationService remediationService,
                               SecurityStateCalculator securityStateCalculator) {
        this.scanExecutionRepository = scanExecutionRepository;
        this.securityFindingRepository = securityFindingRepository;
        this.serviceRepository = serviceRepository;
        this.parserFactory = parserFactory;
        this.findingNormalizer = findingNormalizer;
        this.deduplicationEngine = deduplicationEngine;
        this.prioritizationEngine = prioritizationEngine;
        this.scanComparisonEngine = scanComparisonEngine;
        this.remediationService = remediationService;
        this.securityStateCalculator = securityStateCalculator;
    }

    public ScanExecution processScanExecution(MultipartFile file, String serviceName, Environment environment,
                                            TriggerType triggerType, String repository, String branch,
                                            String commitId, String workflowRunId) 
            throws SecurityReportParseException, IOException {
        
        // Create scan execution record
        ScanExecution scanExecution = new ScanExecution();
        scanExecution.setServiceName(serviceName);
        scanExecution.setEnvironment(environment);
        scanExecution.setTriggerType(triggerType);
        scanExecution.setRepository(repository);
        scanExecution.setBranch(branch);
        scanExecution.setCommitId(commitId);
        scanExecution.setStartedAt(LocalDateTime.now());
        scanExecution.setStatus(Status.PROCESSING);
        
        try {
            scanExecution = scanExecutionRepository.save(scanExecution);
        } catch (Exception e) {
            throw new DatabaseException("Failed to create scan execution", e);
        }

        try {
            // Read and parse the report
            String reportContent = new String(file.getBytes());
            Optional<SecurityReportParser> parser = parserFactory.getParser(reportContent);
            if (parser.isEmpty()) {
                scanExecution.setStatus(Status.FAILED);
                scanExecution.setCompletedAt(LocalDateTime.now());
                scanExecutionRepository.save(scanExecution);
                throw new SecurityReportParseException("No parser found for the uploaded report format");
            }

            ParsedSecurityReport parsedReport = parser.get().parse(reportContent, serviceName, environment);
            
            // Update scan execution with tool and scan type
            scanExecution.setTool(parsedReport.getTool());
            scanExecution.setScanType(parsedReport.getScanType());
            scanExecution.setTotalRawFindings(parsedReport.getFindings().size());
            
            // Normalize findings
            List<SecurityFinding> normalizedFindings = findingNormalizer.normalize(parsedReport, scanExecution.getId());

            // Compare with previous scan
            ScanComparisonResult comparisonResult = scanComparisonEngine.compareWithPreviousScan(
                normalizedFindings, scanExecution);

            // Deduplicate findings
            DeduplicationResult deduplicationResult = deduplicationEngine.processFindings(normalizedFindings);

            // Apply service context and prioritization
            Optional<Service> serviceEntity = serviceRepository.findByServiceName(serviceName);
            for (SecurityFinding finding : deduplicationResult.getUniqueFindings()) {
                PriorityResult priorityResult = prioritizationEngine.calculatePriority(finding, serviceEntity.orElse(null));
                finding.setRiskScore(priorityResult.getRiskScore());
                finding.setPriority(priorityResult.getPriority());
                finding.setPriorityReasons(priorityResult.getReasons());
            }

            // Save findings
            securityFindingRepository.saveAll(deduplicationResult.getUniqueFindings());
            
            // Save resolved findings
            securityFindingRepository.saveAll(comparisonResult.getResolvedFindings());

            // Create or update remediation items
            for (SecurityFinding finding : deduplicationResult.getUniqueFindings()) {
                if (finding.getStatus() == Status.OPEN) {
                    remediationService.createOrUpdateRemediationItem(finding);
                }
            }

            // Update scan execution with results
            scanExecution.setTotalUniqueFindings(deduplicationResult.getUniqueCount());
            scanExecution.setNewFindings(comparisonResult.getNewFindings().size());
            scanExecution.setResolvedFindings(comparisonResult.getResolvedFindings().size());
            scanExecution.setUnchangedFindings(comparisonResult.getUnchangedFindings().size());
            
            // Count severities
            scanExecution.setCriticalCount((int) deduplicationResult.getUniqueFindings().stream()
                .filter(f -> f.getSeverity() == Severity.CRITICAL).count());
            scanExecution.setHighCount((int) deduplicationResult.getUniqueFindings().stream()
                .filter(f -> f.getSeverity() == Severity.HIGH).count());
            scanExecution.setMediumCount((int) deduplicationResult.getUniqueFindings().stream()
                .filter(f -> f.getSeverity() == Severity.MEDIUM).count());
            scanExecution.setLowCount((int) deduplicationResult.getUniqueFindings().stream()
                .filter(f -> f.getSeverity() == Severity.LOW).count());

            scanExecution.setStatus(Status.SUCCESS);
            scanExecution.setCompletedAt(LocalDateTime.now());
            scanExecutionRepository.save(scanExecution);

            return scanExecution;

        } catch (Exception e) {
            scanExecution.setStatus(Status.FAILED);
            scanExecution.setCompletedAt(LocalDateTime.now());
            scanExecutionRepository.save(scanExecution);
            throw e;
        }
    }

    public List<ScanExecution> getAllScanExecutions() {
        try {
            return scanExecutionRepository.findAll();
        } catch (Exception e) {
            throw new DatabaseException("Failed to retrieve scan executions", e);
        }
    }

    public ScanExecution getScanExecutionById(String id) {
        return scanExecutionRepository.findById(id)
            .orElseThrow(() -> new com.securityintel.exception.ResourceNotFoundException("Scan execution not found with id: " + id));
    }

    public List<ScanExecution> getScanExecutionsByService(String serviceName) {
        try {
            return scanExecutionRepository.findByServiceNameOrderByCreatedAtDesc(serviceName);
        } catch (Exception e) {
            throw new DatabaseException("Failed to retrieve scan executions for service: " + serviceName, e);
        }
    }

    public List<ScanExecution> getRecentScanExecutions(int hours) {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusHours(hours);
            return scanExecutionRepository.findByCreatedAtAfterOrderByCreatedAtDesc(cutoff);
        } catch (Exception e) {
            throw new DatabaseException("Failed to retrieve recent scan executions", e);
        }
    }

    public StaleServicesSummary getStaleServicesSummary() {
        try {
            List<Service> allServices = serviceRepository.findAll();
            long staleCount = 0;
            
            for (Service service : allServices) {
                var latestScanOpt = scanExecutionRepository.findLatestSuccessfulScanByService(service.getServiceName());
                if (latestScanOpt.isPresent()) {
                    LocalDateTime scanTime = latestScanOpt.get().getCompletedAt() != null ? 
                        latestScanOpt.get().getCompletedAt() : latestScanOpt.get().getCreatedAt();
                    if (securityStateCalculator.isScanStale(service, scanTime)) {
                        staleCount++;
                    }
                } else {
                    staleCount++; // No scan counts as stale
                }
            }
            
            return new StaleServicesSummary(staleCount, allServices.size());
        } catch (Exception e) {
            throw new DatabaseException("Failed to generate stale services summary", e);
        }
    }

    public static class StaleServicesSummary {
        private final long staleCount;
        private final long totalServices;

        public StaleServicesSummary(long staleCount, long totalServices) {
            this.staleCount = staleCount;
            this.totalServices = totalServices;
        }

        public long getStaleCount() {
            return staleCount;
        }

        public long getTotalServices() {
            return totalServices;
        }
    }
}