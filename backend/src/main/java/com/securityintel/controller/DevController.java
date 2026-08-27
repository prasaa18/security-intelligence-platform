package com.securityintel.controller;

import com.securityintel.dto.ServiceDto;
import com.securityintel.model.*;
import com.securityintel.repository.RemediationItemRepository;
import com.securityintel.repository.ScanExecutionRepository;
import com.securityintel.repository.ScanReportRepository;
import com.securityintel.repository.SecurityFindingRepository;
import com.securityintel.repository.ServiceRepository;
import com.securityintel.service.ServiceManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/dev")
@CrossOrigin(origins = "*")
public class DevController {

    private final ServiceManagementService serviceManagementService;
    private final SecurityFindingRepository securityFindingRepository;
    private final RemediationItemRepository remediationItemRepository;
    private final ScanExecutionRepository scanExecutionRepository;
    private final ServiceRepository serviceRepository;
    private final ScanReportRepository scanReportRepository;

    public DevController(ServiceManagementService serviceManagementService,
                       SecurityFindingRepository securityFindingRepository,
                       RemediationItemRepository remediationItemRepository,
                       ScanExecutionRepository scanExecutionRepository,
                       ServiceRepository serviceRepository,
                       ScanReportRepository scanReportRepository) {
        this.serviceManagementService = serviceManagementService;
        this.securityFindingRepository = securityFindingRepository;
        this.remediationItemRepository = remediationItemRepository;
        this.scanExecutionRepository = scanExecutionRepository;
        this.serviceRepository = serviceRepository;
        this.scanReportRepository = scanReportRepository;
    }

    @PostMapping("/seed")
    public ResponseEntity<?> seedSampleData() {
        try {
            // First seed services
            List<ServiceDto> sampleServices = createSampleServices();
            List<Service> createdServices = new ArrayList<>();

            for (ServiceDto serviceDto : sampleServices) {
                try {
                    // Check if service already exists
                    try {
                        serviceManagementService.getServiceByName(serviceDto.getServiceName());
                        // Service exists, get it
                        Service existingService = serviceRepository.findByServiceName(serviceDto.getServiceName()).orElse(null);
                        if (existingService != null) {
                            createdServices.add(existingService);
                        }
                        continue;
                    } catch (Exception e) {
                        // Service doesn't exist, create it
                        ServiceDto created = serviceManagementService.createService(serviceDto);
                        Service service = serviceRepository.findByServiceName(created.getServiceName()).orElse(null);
                        if (service != null) {
                            createdServices.add(service);
                        }
                    }
                } catch (Exception e) {
                    // Skip this service if there's an error
                    continue;
                }
            }

            // Create sample security findings
            int findingsCreated = createSampleFindings(createdServices);
            
            // Create sample remediation items
            int remediationItemsCreated = createSampleRemediationItems(createdServices);
            
            // Create sample scan executions
            int scanExecutionsCreated = createSampleScanExecutions(createdServices);

            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Sample data seeded successfully",
                "servicesCreated", createdServices.size(),
                "findingsCreated", findingsCreated,
                "remediationItemsCreated", remediationItemsCreated,
                "scanExecutionsCreated", scanExecutionsCreated
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "message", "Failed to seed sample data: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @DeleteMapping("/purge")
    public ResponseEntity<?> purgeAllData() {
        securityFindingRepository.deleteAll();
        remediationItemRepository.deleteAll();
        scanExecutionRepository.deleteAll();
        scanReportRepository.deleteAll();
        serviceRepository.deleteAll();
        return ResponseEntity.ok(Map.of("success", true, "message", "All services, reports, findings, remediation items, and scan executions purged"));
    }

    private List<ServiceDto> createSampleServices() {
        List<ServiceDto> services = new ArrayList<>();

        // payment-service - Critical production service
        services.add(new ServiceDto(
            "payment-service",
            "Payments Team",
            Environment.PRODUCTION,
            BusinessCriticality.CRITICAL,
            true,
            DataSensitivity.SENSITIVE,
            "payment-service",
            "john.smith@company.com"
        ));

        // order-service - High business criticality
        services.add(new ServiceDto(
            "order-service",
            "Commerce Team",
            Environment.PRODUCTION,
            BusinessCriticality.HIGH,
            true,
            DataSensitivity.CONFIDENTIAL,
            "order-service",
            "jane.doe@company.com"
        ));

        // inventory-service
        services.add(new ServiceDto(
            "inventory-service",
            "Commerce Team",
            Environment.PRODUCTION,
            BusinessCriticality.HIGH,
            false,
            DataSensitivity.INTERNAL,
            "inventory-service",
            "mike.wilson@company.com"
        ));

        // user-service
        services.add(new ServiceDto(
            "user-service",
            "Identity Team",
            Environment.PRODUCTION,
            BusinessCriticality.CRITICAL,
            true,
            DataSensitivity.SENSITIVE,
            "user-service",
            "sarah.johnson@company.com"
        ));

        // auth-service
        services.add(new ServiceDto(
            "auth-service",
            "Identity Team",
            Environment.PRODUCTION,
            BusinessCriticality.CRITICAL,
            true,
            DataSensitivity.SENSITIVE,
            "auth-service",
            "alex.brown@company.com"
        ));

        // gateway-service
        services.add(new ServiceDto(
            "gateway-service",
            "Platform Team",
            Environment.PRODUCTION,
            BusinessCriticality.CRITICAL,
            true,
            DataSensitivity.INTERNAL,
            "gateway-service",
            "emily.davis@company.com"
        ));

        // catalog-service
        services.add(new ServiceDto(
            "catalog-service",
            "Commerce Team",
            Environment.PRODUCTION,
            BusinessCriticality.MEDIUM,
            true,
            DataSensitivity.PUBLIC,
            "catalog-service",
            "chris.taylor@company.com"
        ));

        // shipping-service
        services.add(new ServiceDto(
            "shipping-service",
            "Logistics Team",
            Environment.PRODUCTION,
            BusinessCriticality.HIGH,
            false,
            DataSensitivity.CONFIDENTIAL,
            "shipping-service",
            "lisa.anderson@company.com"
        ));

        // notification-service
        services.add(new ServiceDto(
            "notification-service",
            "Platform Team",
            Environment.PRODUCTION,
            BusinessCriticality.MEDIUM,
            false,
            DataSensitivity.INTERNAL,
            "notification-service",
            "david.martinez@company.com"
        ));

        // reporting-service
        services.add(new ServiceDto(
            "reporting-service",
            "Analytics Team",
            Environment.PRODUCTION,
            BusinessCriticality.LOW,
            false,
            DataSensitivity.INTERNAL,
            "reporting-service",
            "jennifer.garcia@company.com"
        ));

        return services;
    }

    private int createSampleFindings(List<Service> services) {
        int count = 0;
        LocalDateTime now = LocalDateTime.now();
        
        for (Service service : services) {
            // Create different types of findings based on service criticality
            int numFindings = service.getBusinessCriticality() == BusinessCriticality.CRITICAL ? 5 : 3;
            
            for (int i = 0; i < numFindings; i++) {
                SecurityFinding finding = new SecurityFinding();
                finding.setId(UUID.randomUUID().toString());
                finding.setServiceName(service.getServiceName());
                finding.setEnvironment(service.getEnvironment());
                finding.setTool(Tool.TRIVY);
                finding.setScanType(ScanType.CONTAINER);
                finding.setTitle("Sample CVE-" + (2024 + i) + "-" + (1000 + i));
                finding.setCve("CVE-" + (2024 + i) + "-" + (1000 + i));
                finding.setSeverity(i == 0 ? Severity.CRITICAL : i == 1 ? Severity.HIGH : Severity.MEDIUM);
                finding.setCvssScore(i == 0 ? 9.8 : i == 1 ? 7.5 : 5.5);
                finding.setPackageName("vulnerable-package-" + i);
                finding.setInstalledVersion("1.0." + i);
                finding.setFixedVersion("1.0." + (i + 1));
                finding.setDescription("Sample vulnerability description for testing purposes");
                finding.setStatus(Status.OPEN);
                
                // Calculate priority based on service context
                Priority priority = calculatePriority(service, finding.getSeverity().name());
                finding.setPriority(priority);
                finding.setRiskScore(calculateRiskScore(service, finding.getSeverity().name()));
                
                // Add priority reasons
                List<String> reasons = new ArrayList<>();
                reasons.add("Scanner severity: " + finding.getSeverity().name());
                if (service.isInternetExposed()) {
                    reasons.add("Internet exposed service");
                }
                if (service.getBusinessCriticality() == BusinessCriticality.CRITICAL) {
                    reasons.add("Business critical service");
                }
                finding.setPriorityReasons(reasons);
                
                finding.setFirstDetectedAt(now.minusDays(i));
                finding.setLastDetectedAt(now.minusHours(i * 2));
                finding.setFingerprint(generateFingerprint(finding));
                
                try {
                    securityFindingRepository.save(finding);
                    count++;
                } catch (Exception e) {
                    // Skip if save fails
                }
            }
        }
        return count;
    }

    private int createSampleRemediationItems(List<Service> services) {
        int count = 0;
        LocalDateTime now = LocalDateTime.now();
        
        List<SecurityFinding> findings = securityFindingRepository.findAll();
        
        for (SecurityFinding finding : findings) {
            if (count >= 20) break; // Limit to 20 items
            
            RemediationItem item = new RemediationItem();
            item.setId(UUID.randomUUID().toString());
            item.setFindingId(finding.getId());
            item.setServiceName(finding.getServiceName());
            
            // Get team from service
            Service service = serviceRepository.findByServiceName(finding.getServiceName()).orElse(null);
            if (service != null) {
                item.setTeamName(service.getTeamName());
            }
            
            item.setPriority(finding.getPriority());
            item.setRiskScore(finding.getRiskScore());
            item.setRemediationStatus(count < 5 ? RemediationStatus.OPEN : 
                                      count < 10 ? RemediationStatus.IN_PROGRESS : 
                                      RemediationStatus.NEW);
            item.setRecommendedAction("Update " + finding.getPackageName() + " to version " + finding.getFixedVersion());
            item.setFirstDetectedAt(finding.getFirstDetectedAt());
            item.setLastDetectedAt(finding.getLastDetectedAt());
            item.setLatestScanAt(now.minusHours(count));
            
            if (item.getRemediationStatus() == RemediationStatus.RESOLVED) {
                item.setResolvedAt(now.minusDays(1));
            }
            
            try {
                remediationItemRepository.save(item);
                count++;
            } catch (Exception e) {
                // Skip if save fails
            }
        }
        return count;
    }

    private int createSampleScanExecutions(List<Service> services) {
        int count = 0;
        LocalDateTime now = LocalDateTime.now();
        
        for (Service service : services) {
            // Create 2-3 scan executions per service
            for (int i = 0; i < 2; i++) {
                ScanExecution scan = new ScanExecution();
                scan.setId(UUID.randomUUID().toString());
                scan.setServiceName(service.getServiceName());
                scan.setEnvironment(service.getEnvironment());
                scan.setTool(Tool.TRIVY);
                scan.setScanType(ScanType.CONTAINER);
                scan.setTriggerType(TriggerType.MANUAL_UPLOAD);
                scan.setStatus(i == 0 ? Status.SUCCESS : Status.SUCCESS);
                scan.setCreatedAt(now.minusDays(i));
                scan.setCompletedAt(now.minusDays(i).plusMinutes(5));
                
                // Set scan statistics
                scan.setTotalRawFindings(5 + i);
                scan.setCriticalCount(i == 0 ? 1 : 0);
                scan.setHighCount(2);
                scan.setMediumCount(2);
                scan.setLowCount(1);
                scan.setTotalUniqueFindings(4 + i);
                
                // Set change statistics
                scan.setNewFindings(i == 0 ? 5 : 0);
                scan.setResolvedFindings(i == 0 ? 0 : 1);
                scan.setUnchangedFindings(4);
                
                try {
                    scanExecutionRepository.save(scan);
                    count++;
                } catch (Exception e) {
                    // Skip if save fails
                }
            }
        }
        return count;
    }

    private Priority calculatePriority(Service service, String severity) {
        if (severity.equals("CRITICAL") && service.getEnvironment() == Environment.PRODUCTION) {
            return Priority.P0;
        } else if (severity.equals("HIGH") && service.getEnvironment() == Environment.PRODUCTION) {
            return service.isInternetExposed() ? Priority.P0 : Priority.P1;
        } else if (severity.equals("CRITICAL")) {
            return Priority.P1;
        } else if (severity.equals("HIGH")) {
            return Priority.P2;
        } else if (severity.equals("MEDIUM")) {
            return service.getBusinessCriticality() == BusinessCriticality.CRITICAL ? Priority.P2 : Priority.P3;
        }
        return Priority.P4;
    }

    private int calculateRiskScore(Service service, String severity) {
        int baseScore = switch (severity) {
            case "CRITICAL" -> 90;
            case "HIGH" -> 70;
            case "MEDIUM" -> 50;
            case "LOW" -> 30;
            default -> 20;
        };
        
        // Add context factors
        if (service.isInternetExposed()) baseScore += 10;
        if (service.getBusinessCriticality() == BusinessCriticality.CRITICAL) baseScore += 10;
        if (service.getEnvironment() == Environment.PRODUCTION) baseScore += 5;
        if (service.getDataSensitivity() == DataSensitivity.SENSITIVE) baseScore += 5;
        
        return Math.min(baseScore, 100);
    }

    private String generateFingerprint(SecurityFinding finding) {
        return finding.getCve() + "|" + finding.getPackageName() + "|" + finding.getInstalledVersion();
    }
}