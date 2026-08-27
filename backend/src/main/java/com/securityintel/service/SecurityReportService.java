package com.securityintel.service;

import com.securityintel.deduplication.DeduplicationEngine;
import com.securityintel.deduplication.DeduplicationResult;
import com.securityintel.dto.ScanReportDto;
import com.securityintel.exception.DatabaseException;
import com.securityintel.exception.UnsupportedReportException;
import com.securityintel.mapper.EntityMapper;
import com.securityintel.model.*;
import com.securityintel.normalization.FindingNormalizer;
import com.securityintel.parser.ParsedSecurityReport;
import com.securityintel.parser.SecurityReportParser;
import com.securityintel.parser.SecurityReportParseException;
import com.securityintel.parser.SecurityReportParserFactory;
import com.securityintel.prioritization.PriorityResult;
import com.securityintel.prioritization.SecurityPrioritizationEngine;
import com.securityintel.remediation.RemediationService;
import com.securityintel.repository.ScanReportRepository;
import com.securityintel.repository.ScanExecutionRepository;
import com.securityintel.repository.SecurityFindingRepository;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class SecurityReportService {

    private final ScanReportRepository scanReportRepository;
    private final SecurityFindingRepository securityFindingRepository;
    private final SecurityReportParserFactory parserFactory;
    private final FindingNormalizer findingNormalizer;
    private final DeduplicationEngine deduplicationEngine;
    private final SecurityPrioritizationEngine prioritizationEngine;
    private final ServiceManagementService serviceManagementService;
    private final EntityMapper entityMapper;
    private final ScanExecutionRepository scanExecutionRepository;
    private final RemediationService remediationService;

    @Autowired
    public SecurityReportService(ScanReportRepository scanReportRepository,
                               SecurityFindingRepository securityFindingRepository,
                               SecurityReportParserFactory parserFactory,
                               FindingNormalizer findingNormalizer,
                               DeduplicationEngine deduplicationEngine,
                               SecurityPrioritizationEngine prioritizationEngine,
                               ServiceManagementService serviceManagementService,
                               EntityMapper entityMapper,
                               ScanExecutionRepository scanExecutionRepository,
                               RemediationService remediationService) {
        this.scanReportRepository = scanReportRepository;
        this.securityFindingRepository = securityFindingRepository;
        this.parserFactory = parserFactory;
        this.findingNormalizer = findingNormalizer;
        this.deduplicationEngine = deduplicationEngine;
        this.prioritizationEngine = prioritizationEngine;
        this.serviceManagementService = serviceManagementService;
        this.entityMapper = entityMapper;
        this.scanExecutionRepository = scanExecutionRepository;
        this.remediationService = remediationService;
    }

    public SecurityReportService(ScanReportRepository scanReportRepository,
                               SecurityFindingRepository securityFindingRepository,
                               SecurityReportParserFactory parserFactory,
                               FindingNormalizer findingNormalizer,
                               DeduplicationEngine deduplicationEngine,
                               SecurityPrioritizationEngine prioritizationEngine,
                               ServiceManagementService serviceManagementService,
                               EntityMapper entityMapper) {
        this(scanReportRepository, securityFindingRepository, parserFactory, findingNormalizer,
            deduplicationEngine, prioritizationEngine, serviceManagementService, entityMapper, null, null);
    }

    public ReportProcessingResult processSecurityReport(MultipartFile file, String serviceName, Environment environment) 
            throws SecurityReportParseException, IOException {
        
        // Read file content
        String reportContent = new String(file.getBytes());
        
        // Find appropriate parser
        Optional<SecurityReportParser> parser = parserFactory.getParser(reportContent);
        if (parser.isEmpty()) {
            throw new UnsupportedReportException("No parser found for the uploaded report format");
        }

        // Parse the report
        ParsedSecurityReport parsedReport = parser.get().parse(reportContent, serviceName, environment);
        
        // Create and save scan report record
        ScanReport scanReport = new ScanReport(
            parsedReport.getTool(),
            parsedReport.getScanType(),
            serviceName,
            environment,
            file.getOriginalFilename(),
            parsedReport.getFindings().size()
        );
        scanReport.setRepository(parsedReport.getRepository());
        scanReport.setBranch(parsedReport.getBranch());
        scanReport.setCommitId(parsedReport.getCommitId());
        scanReport.setRawReportContent(reportContent);
        
        try {
            scanReport = scanReportRepository.save(scanReport);
        } catch (Exception e) {
            throw new DatabaseException("Failed to save scan report", e);
        }

        ScanExecution scanExecution = new ScanExecution();
        scanExecution.setServiceName(serviceName);
        scanExecution.setEnvironment(environment);
        scanExecution.setTriggerType(TriggerType.MANUAL_UPLOAD);
        scanExecution.setTool(parsedReport.getTool());
        scanExecution.setScanType(parsedReport.getScanType());
        scanExecution.setRepository(parsedReport.getRepository());
        scanExecution.setBranch(parsedReport.getBranch());
        scanExecution.setCommitId(parsedReport.getCommitId());
        scanExecution.setStartedAt(LocalDateTime.now());
        scanExecution.setStatus(Status.PROCESSING);
        scanExecution = scanExecutionRepository.save(scanExecution);

        // Normalize findings
        List<SecurityFinding> normalizedFindings = findingNormalizer.normalize(parsedReport, scanExecution.getId());

        // Deduplicate findings
        DeduplicationResult deduplicationResult = deduplicationEngine.processFindings(normalizedFindings);

        // Apply prioritization
        Optional<Service> serviceEntity = serviceManagementService.findServiceEntityByName(serviceName);
        for (SecurityFinding finding : deduplicationResult.getUniqueFindings()) {
            PriorityResult priorityResult = prioritizationEngine.calculatePriority(finding, serviceEntity.orElse(null));
            finding.setRiskScore(priorityResult.getRiskScore());
            finding.setPriority(priorityResult.getPriority());
            finding.setPriorityReasons(priorityResult.getReasons());
        }

        // Save unique findings
        try {
            securityFindingRepository.saveAll(deduplicationResult.getUniqueFindings());
        } catch (Exception e) {
            throw new DatabaseException("Failed to save security findings", e);
        }

        for (SecurityFinding finding : deduplicationResult.getUniqueFindings()) {
            if (finding.getDetectionState() == null) finding.setDetectionState(DetectionState.NEW);
            if (finding.getFirstDetectedAt() == null) finding.setFirstDetectedAt(LocalDateTime.now());
            finding.setLastDetectedAt(LocalDateTime.now());
            finding.setLatestScanId(scanExecution.getId());
            if (finding.getStatus() == Status.OPEN) remediationService.createOrUpdateRemediationItem(finding);
        }

        scanExecution.setTotalRawFindings(deduplicationResult.getTotalRawFindings());
        scanExecution.setTotalUniqueFindings(deduplicationResult.getUniqueCount());
        scanExecution.setNewFindings(deduplicationResult.getUniqueCount());
        scanExecution.setCriticalCount((int) deduplicationResult.getUniqueFindings().stream().filter(f -> f.getSeverity() == Severity.CRITICAL).count());
        scanExecution.setHighCount((int) deduplicationResult.getUniqueFindings().stream().filter(f -> f.getSeverity() == Severity.HIGH).count());
        scanExecution.setMediumCount((int) deduplicationResult.getUniqueFindings().stream().filter(f -> f.getSeverity() == Severity.MEDIUM).count());
        scanExecution.setLowCount((int) deduplicationResult.getUniqueFindings().stream().filter(f -> f.getSeverity() == Severity.LOW).count());
        scanExecution.setStatus(Status.SUCCESS);
        scanExecution.setCompletedAt(LocalDateTime.now());
        scanExecutionRepository.save(scanExecution);

        return new ReportProcessingResult(
            entityMapper.toDto(scanReport),
            parsedReport.getTool(),
            deduplicationResult.getTotalRawFindings(),
            deduplicationResult.getUniqueCount()
        );
    }

    public List<ScanReportDto> getAllReports() {
        try {
            List<ScanReport> reports = scanReportRepository.findAll();
            return entityMapper.toReportDtos(reports);
        } catch (Exception e) {
            throw new DatabaseException("Failed to retrieve scan reports", e);
        }
    }

    public ScanReportDto getReportById(String id) {
        ScanReport report = scanReportRepository.findById(id)
            .orElseThrow(() -> new com.securityintel.exception.ResourceNotFoundException("Report not found with id: " + id));
        return entityMapper.toDto(report);
    }

    public ScanReportDownload getReportDownload(String id) {
        ScanReport report = scanReportRepository.findById(id)
            .orElseThrow(() -> new com.securityintel.exception.ResourceNotFoundException("Report not found with id: " + id));
        if (report.getRawReportContent() == null || report.getRawReportContent().isBlank()) {
            throw new com.securityintel.exception.ResourceNotFoundException("Original report content is unavailable");
        }
        return new ScanReportDownload(report.getUploadedFileName(), report.getRawReportContent());
    }

    public record ScanReportDownload(String fileName, String content) {}

    public List<ScanReportDto> getReportsByService(String serviceName) {
        try {
            List<ScanReport> reports = scanReportRepository.findByServiceName(serviceName);
            return entityMapper.toReportDtos(reports);
        } catch (Exception e) {
            throw new DatabaseException("Failed to retrieve reports for service: " + serviceName, e);
        }
    }

    // Result class for report processing
    public static class ReportProcessingResult {
        private final ScanReportDto scanReport;
        private final Tool tool;
        private final int rawFindings;
        private final int uniqueFindings;

        public ReportProcessingResult(ScanReportDto scanReport, Tool tool, int rawFindings, int uniqueFindings) {
            this.scanReport = scanReport;
            this.tool = tool;
            this.rawFindings = rawFindings;
            this.uniqueFindings = uniqueFindings;
        }

        public ScanReportDto getScanReport() {
            return scanReport;
        }

        public Tool getTool() {
            return tool;
        }

        public int getRawFindings() {
            return rawFindings;
        }

        public int getUniqueFindings() {
            return uniqueFindings;
        }
    }
}