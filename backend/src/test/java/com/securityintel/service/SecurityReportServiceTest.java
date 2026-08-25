package com.securityintel.service;

import com.securityintel.deduplication.DeduplicationEngine;
import com.securityintel.deduplication.DeduplicationResult;
import com.securityintel.dto.ScanReportDto;
import com.securityintel.mapper.EntityMapper;
import com.securityintel.model.*;
import com.securityintel.normalization.FindingNormalizer;
import com.securityintel.parser.ParsedFinding;
import com.securityintel.parser.ParsedSecurityReport;
import com.securityintel.parser.SecurityReportParser;
import com.securityintel.parser.SecurityReportParserFactory;
import com.securityintel.prioritization.PriorityResult;
import com.securityintel.prioritization.SecurityPrioritizationEngine;
import com.securityintel.repository.ScanReportRepository;
import com.securityintel.repository.SecurityFindingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityReportServiceTest {

    @Mock
    private ScanReportRepository scanReportRepository;

    @Mock
    private SecurityFindingRepository securityFindingRepository;

    @Mock
    private SecurityReportParserFactory parserFactory;

    @Mock
    private FindingNormalizer findingNormalizer;

    @Mock
    private DeduplicationEngine deduplicationEngine;

    @Mock
    private SecurityPrioritizationEngine prioritizationEngine;

    @Mock
    private ServiceManagementService serviceManagementService;

    @Mock
    private EntityMapper entityMapper;

    @Mock
    private SecurityReportParser mockParser;

    private SecurityReportService securityReportService;

    @BeforeEach
    void setUp() {
        securityReportService = new SecurityReportService(
            scanReportRepository,
            securityFindingRepository,
            parserFactory,
            findingNormalizer,
            deduplicationEngine,
            prioritizationEngine,
            serviceManagementService,
            entityMapper
        );
    }

    @Test
    @DisplayName("Should process security report end-to-end: parse, normalize, deduplicate, prioritize and persist")
    void shouldProcessSecurityReportEndToEnd() throws Exception {
        String reportJson = "{\"SchemaVersion\": 2, \"Results\": []}";
        MockMultipartFile file = new MockMultipartFile("file", "trivy.json", "application/json", reportJson.getBytes());

        ParsedSecurityReport parsedReport = new ParsedSecurityReport();
        parsedReport.setTool(Tool.TRIVY);
        parsedReport.setScanType(ScanType.CONTAINER);
        parsedReport.setServiceName("payment-service");
        parsedReport.setEnvironment(Environment.PRODUCTION);
        parsedReport.setFindings(List.of(new ParsedFinding()));

        when(parserFactory.getParser(anyString())).thenReturn(Optional.of(mockParser));
        when(mockParser.parse(anyString(), eq("payment-service"), eq(Environment.PRODUCTION))).thenReturn(parsedReport);

        ScanReport savedReport = new ScanReport(Tool.TRIVY, ScanType.CONTAINER, "payment-service", Environment.PRODUCTION, "trivy.json", 1);
        savedReport.setId("report-123");
        when(scanReportRepository.save(any(ScanReport.class))).thenReturn(savedReport);

        SecurityFinding normalizedFinding = new SecurityFinding();
        normalizedFinding.setCve("CVE-2024-1234");
        normalizedFinding.setSeverity(Severity.CRITICAL);
        when(findingNormalizer.normalize(parsedReport, "report-123")).thenReturn(List.of(normalizedFinding));

        DeduplicationResult dedupResult = new DeduplicationResult(
            new ArrayList<>(List.of(normalizedFinding)),
            new ArrayList<>(),
            new HashMap<>()
        );
        when(deduplicationEngine.processFindings(anyList())).thenReturn(dedupResult);

        Service service = new Service();
        service.setServiceName("payment-service");
        service.setBusinessCriticality(BusinessCriticality.CRITICAL);
        when(serviceManagementService.findServiceEntityByName("payment-service")).thenReturn(Optional.of(service));

        PriorityResult priorityResult = new PriorityResult(96, Priority.P0, List.of("Critical severity", "Production"));
        when(prioritizationEngine.calculatePriority(normalizedFinding, service)).thenReturn(priorityResult);

        ScanReportDto reportDto = new ScanReportDto();
        reportDto.setId("report-123");
        when(entityMapper.toDto(savedReport)).thenReturn(reportDto);

        SecurityReportService.ReportProcessingResult result = 
            securityReportService.processSecurityReport(file, "payment-service", Environment.PRODUCTION);

        assertNotNull(result);
        assertEquals(Tool.TRIVY, result.getTool());
        assertEquals(1, result.getRawFindings());
        assertEquals(1, result.getUniqueFindings());
        assertEquals(96, normalizedFinding.getRiskScore());
        assertEquals(Priority.P0, normalizedFinding.getPriority());

        verify(securityFindingRepository).saveAll(anyList());
    }
}

