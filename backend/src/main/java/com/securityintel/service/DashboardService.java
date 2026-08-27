package com.securityintel.service;

import com.securityintel.dto.DashboardSummaryDto;
import com.securityintel.exception.DatabaseException;
import com.securityintel.mapper.EntityMapper;
import com.securityintel.model.*;
import com.securityintel.repository.ScanReportRepository;
import com.securityintel.repository.SecurityFindingRepository;
import com.securityintel.remediation.RemediationService;
import com.securityintel.scan.ScanExecutionService;
import com.securityintel.securitystate.SecurityStateCalculator;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DashboardService {

    private final SecurityFindingRepository securityFindingRepository;
    private final ScanReportRepository scanReportRepository;
    private final EntityMapper entityMapper;
    private final RemediationService remediationService;
    private final ScanExecutionService scanExecutionService;
    private final SecurityStateCalculator securityStateCalculator;

    public DashboardService(SecurityFindingRepository securityFindingRepository,
                          ScanReportRepository scanReportRepository,
                          EntityMapper entityMapper,
                          RemediationService remediationService,
                          ScanExecutionService scanExecutionService,
                          SecurityStateCalculator securityStateCalculator) {
        this.securityFindingRepository = securityFindingRepository;
        this.scanReportRepository = scanReportRepository;
        this.entityMapper = entityMapper;
        this.remediationService = remediationService;
        this.scanExecutionService = scanExecutionService;
        this.securityStateCalculator = securityStateCalculator;
    }

    public DashboardSummaryDto getDashboardSummary() {
        try {
            DashboardSummaryDto summary = new DashboardSummaryDto();

            // Total raw findings from reports, and unique findings from repository
            long totalRawFindings = scanReportRepository.findAll().stream()
                .mapToLong(ScanReport::getTotalFindings)
                .sum();
            long uniqueCount = securityFindingRepository.count();
            summary.setTotalFindings(totalRawFindings > 0 ? totalRawFindings : uniqueCount);
            summary.setUniqueFindings(uniqueCount);

            // Severity counts
            summary.setCritical(securityFindingRepository.countBySeverity(Severity.CRITICAL));
            summary.setHigh(securityFindingRepository.countBySeverity(Severity.HIGH));
            summary.setMedium(securityFindingRepository.countBySeverity(Severity.MEDIUM));
            summary.setLow(securityFindingRepository.countBySeverity(Severity.LOW));

            // Priority counts
            summary.setP0(securityFindingRepository.countByPriority(Priority.P0));
            summary.setP1(securityFindingRepository.countByPriority(Priority.P1));
            summary.setP2(securityFindingRepository.countByPriority(Priority.P2));
            summary.setP3(securityFindingRepository.countByPriority(Priority.P3));
            summary.setP4(securityFindingRepository.countByPriority(Priority.P4));

            // Top priority findings
            Pageable topFindingsPageable = PageRequest.of(0, 5);
            List<SecurityFinding> topFindings = securityFindingRepository.findTopPriorityFindings(topFindingsPageable);
            List<DashboardSummaryDto.TopPriorityFindingDto> topPriorityDtos = topFindings.stream()
                .map(entityMapper::toTopPriorityDto)
                .toList();
            summary.setTopPriorities(topPriorityDtos);

            // Scanner distribution
            Map<Tool, Long> scannerDist = new HashMap<>();
            for (Tool tool : Tool.values()) {
                long count = securityFindingRepository.countByTool(tool);
                if (count > 0) {
                    scannerDist.put(tool, count);
                }
            }
            summary.setScannerDistribution(scannerDist);

            // Scan type distribution
            Map<ScanType, Long> scanTypeDist = new HashMap<>();
            for (ScanType scanType : ScanType.values()) {
                long count = securityFindingRepository.countByScanType(scanType);
                if (count > 0) {
                    scanTypeDist.put(scanType, count);
                }
            }
            summary.setScanTypeDistribution(scanTypeDist);

            return summary;
        } catch (Exception e) {
            throw new DatabaseException("Failed to generate dashboard summary", e);
        }
    }

    public ActionCenterDashboardDto getActionCenterDashboard() {
        ActionCenterDashboardDto dashboard = new ActionCenterDashboardDto();
        
        try {
            // Get action center summary
            RemediationService.ActionCenterSummary actionSummary = remediationService.getActionCenterSummary();
            dashboard.setImmediateActions(actionSummary.getImmediateActions());
            dashboard.setDueThisWeek(actionSummary.getDueThisWeek());
            dashboard.setRecentlyResolved(actionSummary.getRecentlyResolved());
        } catch (Exception e) {
            dashboard.setImmediateActions(0);
            dashboard.setDueThisWeek(0);
            dashboard.setRecentlyResolved(0);
        }
        
        try {
            // Get stale services summary
            ScanExecutionService.StaleServicesSummary staleSummary = scanExecutionService.getStaleServicesSummary();
            dashboard.setStaleServices(staleSummary.getStaleCount());
        } catch (Exception e) {
            dashboard.setStaleServices(0);
        }
        
        try {
            // Get top remediation items
            List<RemediationItem> topItems = remediationService.getTopRemediationItems(10);
            dashboard.setTopRemediationItems(topItems);
        } catch (Exception e) {
            dashboard.setTopRemediationItems(new java.util.ArrayList<>());
        }
        
        try {
            // Get recent scan activity
            List<ScanExecution> recentScans = scanExecutionService.getRecentScanExecutions(24);
            dashboard.setRecentScanActivity(recentScans);
        } catch (Exception e) {
            dashboard.setRecentScanActivity(new java.util.ArrayList<>());
        }
        
        return dashboard;
    }

    public static class ActionCenterDashboardDto {
        private long immediateActions;
        private long dueThisWeek;
        private long staleServices;
        private long recentlyResolved;
        private List<RemediationItem> topRemediationItems;
        private List<ScanExecution> recentScanActivity;

        // Getters and Setters
        public long getImmediateActions() {
            return immediateActions;
        }

        public void setImmediateActions(long immediateActions) {
            this.immediateActions = immediateActions;
        }

        public long getDueThisWeek() {
            return dueThisWeek;
        }

        public void setDueThisWeek(long dueThisWeek) {
            this.dueThisWeek = dueThisWeek;
        }

        public long getStaleServices() {
            return staleServices;
        }

        public void setStaleServices(long staleServices) {
            this.staleServices = staleServices;
        }

        public long getRecentlyResolved() {
            return recentlyResolved;
        }

        public void setRecentlyResolved(long recentlyResolved) {
            this.recentlyResolved = recentlyResolved;
        }

        public List<RemediationItem> getTopRemediationItems() {
            return topRemediationItems;
        }

        public void setTopRemediationItems(List<RemediationItem> topRemediationItems) {
            this.topRemediationItems = topRemediationItems;
        }

        public List<ScanExecution> getRecentScanActivity() {
            return recentScanActivity;
        }

        public void setRecentScanActivity(List<ScanExecution> recentScanActivity) {
            this.recentScanActivity = recentScanActivity;
        }
    }
}