package com.securityintel.service;

import com.securityintel.dto.DashboardSummaryDto;
import com.securityintel.mapper.EntityMapper;
import com.securityintel.model.*;
import com.securityintel.repository.ScanReportRepository;
import com.securityintel.repository.SecurityFindingRepository;
import com.securityintel.remediation.RemediationService;
import com.securityintel.scan.ScanExecutionService;
import com.securityintel.securitystate.SecurityStateCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DashboardServiceTest {

    @Mock
    private SecurityFindingRepository securityFindingRepository;

    @Mock
    private ScanReportRepository scanReportRepository;

    @Mock
    private EntityMapper entityMapper;

    @Mock
    private RemediationService remediationService;

    @Mock
    private ScanExecutionService scanExecutionService;

    @Mock
    private SecurityStateCalculator securityStateCalculator;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(securityFindingRepository, scanReportRepository, entityMapper, 
            remediationService, scanExecutionService, securityStateCalculator);
    }

    @Test
    @DisplayName("Should aggregate dashboard summary counts properly")
    void shouldAggregateDashboardSummary() {
        ScanReport report = new ScanReport(Tool.TRIVY, ScanType.CONTAINER, "payment-service", Environment.PRODUCTION, "trivy.json", 10);
        when(scanReportRepository.findAll()).thenReturn(List.of(report));
        when(securityFindingRepository.count()).thenReturn(8L);

        when(securityFindingRepository.countBySeverity(Severity.CRITICAL)).thenReturn(2L);
        when(securityFindingRepository.countBySeverity(Severity.HIGH)).thenReturn(3L);
        when(securityFindingRepository.countBySeverity(Severity.MEDIUM)).thenReturn(2L);
        when(securityFindingRepository.countBySeverity(Severity.LOW)).thenReturn(1L);

        when(securityFindingRepository.countByPriority(Priority.P0)).thenReturn(2L);
        when(securityFindingRepository.countByPriority(Priority.P1)).thenReturn(3L);
        when(securityFindingRepository.countByPriority(Priority.P2)).thenReturn(2L);
        when(securityFindingRepository.countByPriority(Priority.P3)).thenReturn(1L);
        when(securityFindingRepository.countByPriority(Priority.P4)).thenReturn(0L);

        when(securityFindingRepository.countByTool(Tool.TRIVY)).thenReturn(8L);
        when(securityFindingRepository.countByScanType(ScanType.CONTAINER)).thenReturn(8L);

        when(securityFindingRepository.findTopPriorityFindings(any(Pageable.class))).thenReturn(List.of());

        DashboardSummaryDto summary = dashboardService.getDashboardSummary();

        assertNotNull(summary);
        assertEquals(10L, summary.getTotalFindings());
        assertEquals(8L, summary.getUniqueFindings());
        assertEquals(2L, summary.getCritical());
        assertEquals(3L, summary.getHigh());
        assertEquals(2L, summary.getP0());
        assertEquals(3L, summary.getP1());
        assertEquals(8L, summary.getScannerDistribution().get(Tool.TRIVY));
        assertEquals(8L, summary.getScanTypeDistribution().get(ScanType.CONTAINER));
    }
}
