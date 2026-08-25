package com.securityintel.securitystate;

import com.securityintel.model.Environment;
import com.securityintel.model.Priority;
import com.securityintel.model.SecurityState;
import com.securityintel.model.Service;
import com.securityintel.repository.ScanExecutionRepository;
import com.securityintel.repository.SecurityFindingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class SecurityStateCalculator {

    private final SecurityFindingRepository securityFindingRepository;
    private final ScanExecutionRepository scanExecutionRepository;
    
    @Value("${scan.freshness.production.hours:24}")
    private int productionFreshnessHours;
    
    @Value("${scan.freshness.development.hours:168}")
    private int developmentFreshnessHours;

    public SecurityStateCalculator(SecurityFindingRepository securityFindingRepository,
                                   ScanExecutionRepository scanExecutionRepository) {
        this.securityFindingRepository = securityFindingRepository;
        this.scanExecutionRepository = scanExecutionRepository;
    }

    public SecurityState calculateSecurityState(Service service) {
        // Check if service has any successful scans
        var latestScanOpt = scanExecutionRepository.findLatestSuccessfulScanByService(service.getServiceName());
        
        if (latestScanOpt.isEmpty()) {
            return SecurityState.UNKNOWN;
        }
        
        var latestScan = latestScanOpt.get();
        
        // Check scan freshness
        if (isScanStale(service, latestScan.getCompletedAt() != null ? latestScan.getCompletedAt() : latestScan.getCreatedAt())) {
            return SecurityState.STALE;
        }
        
        // Check for critical findings in this service
        long openP0Count = securityFindingRepository.findByServiceNameAndPriorityAndStatus(
            service.getServiceName(), Priority.P0, com.securityintel.model.Status.OPEN).size();
        
        if (openP0Count > 0) {
            return SecurityState.CRITICAL;
        }
        
        // Check for significant P1 findings in this service
        long openP1Count = securityFindingRepository.findByServiceNameAndPriorityAndStatus(
            service.getServiceName(), Priority.P1, com.securityintel.model.Status.OPEN).size();
        
        if (openP1Count > 0) {
            return SecurityState.ATTENTION;
        }
        
        // Check for significant P2 findings in critical services
        if (service.getBusinessCriticality() == com.securityintel.model.BusinessCriticality.CRITICAL) {
            long openP2Count = securityFindingRepository.findByServiceNameAndPriorityAndStatus(
                service.getServiceName(), Priority.P2, com.securityintel.model.Status.OPEN).size();
            
            if (openP2Count > 0) {
                return SecurityState.ATTENTION;
            }
        }
        
        return SecurityState.HEALTHY;
    }

    public boolean isScanStale(Service service, LocalDateTime scanTime) {
        if (scanTime == null) {
            return true;
        }
        
        long hoursSinceScan = Duration.between(scanTime, LocalDateTime.now()).toHours();
        
        if (service.getEnvironment() == Environment.PRODUCTION) {
            return hoursSinceScan > productionFreshnessHours;
        } else {
            return hoursSinceScan > developmentFreshnessHours;
        }
    }

    public String getFreshnessStatus(Service service, LocalDateTime scanTime) {
        if (scanTime == null) {
            return "NO_SCAN";
        }
        
        if (isScanStale(service, scanTime)) {
            return "STALE";
        }
        
        return "FRESH";
    }
}