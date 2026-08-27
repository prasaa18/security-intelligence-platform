package com.securityintel.controller;

import com.securityintel.dto.ServiceDto;
import com.securityintel.model.*;
import com.securityintel.repository.RemediationItemRepository;
import com.securityintel.repository.ScanExecutionRepository;
import com.securityintel.repository.SecurityFindingRepository;
import com.securityintel.repository.ScanReportRepository;
import com.securityintel.service.ServiceManagementService;
import com.securityintel.service.SecurityNotificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/services")
@CrossOrigin(origins = "*")
public class ServiceController {

    private final ServiceManagementService serviceManagementService;
    private final SecurityFindingRepository securityFindingRepository;
    private final RemediationItemRepository remediationItemRepository;
    private final ScanExecutionRepository scanExecutionRepository;
    private final ScanReportRepository scanReportRepository;
    private final SecurityNotificationService notificationService;

    public ServiceController(ServiceManagementService serviceManagementService,
                           SecurityFindingRepository securityFindingRepository,
                           RemediationItemRepository remediationItemRepository,
                           ScanExecutionRepository scanExecutionRepository,
                           SecurityNotificationService notificationService,
                           ScanReportRepository scanReportRepository) {
        this.serviceManagementService = serviceManagementService;
        this.securityFindingRepository = securityFindingRepository;
        this.remediationItemRepository = remediationItemRepository;
        this.scanExecutionRepository = scanExecutionRepository;
        this.notificationService = notificationService;
        this.scanReportRepository = scanReportRepository;
    }

    @GetMapping
    public ResponseEntity<List<ServiceDto>> getAllServices() {
        List<ServiceDto> services = serviceManagementService.getAllServices();
        return ResponseEntity.ok(services);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceDto> getServiceById(@PathVariable String id) {
        ServiceDto service = serviceManagementService.getServiceById(id);
        return ResponseEntity.ok(service);
    }

    @GetMapping("/by-name/{serviceName}")
    public ResponseEntity<ServiceDto> getServiceByName(@PathVariable String serviceName) {
        ServiceDto service = serviceManagementService.getServiceByName(serviceName);
        return ResponseEntity.ok(service);
    }

    @PostMapping
    public ResponseEntity<ServiceDto> createService(@Valid @RequestBody ServiceDto serviceDto) {
        ServiceDto createdService = serviceManagementService.createService(serviceDto);
        return new ResponseEntity<>(createdService, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceDto> updateService(@PathVariable String id, 
                                                   @Valid @RequestBody ServiceDto serviceDto) {
        ServiceDto updatedService = serviceManagementService.updateService(id, serviceDto);
        return ResponseEntity.ok(updatedService);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable String id) {
        serviceManagementService.deleteService(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<ServiceDto>> searchServices(@RequestParam String query) {
        List<ServiceDto> services = serviceManagementService.searchServices(query);
        return ResponseEntity.ok(services);
    }

    @GetMapping("/{serviceName}/security-summary")
    public ResponseEntity<Map<String, Object>> getServiceSecuritySummary(@PathVariable String serviceName) {
        // Get all findings for this service
        List<SecurityFinding> findings = securityFindingRepository.findByServiceName(serviceName);
        
        // Get remediation items for this service
        List<RemediationItem> remediationItems = remediationItemRepository.findByServiceName(serviceName);
        
        // Get latest scan for this service
        var latestScan = scanExecutionRepository.findLatestSuccessfulScanByService(serviceName);
        
        // Calculate summary statistics
        Map<Priority, Long> priorityCounts = findings.stream()
            .collect(Collectors.groupingBy(SecurityFinding::getPriority, Collectors.counting()));
        
        Map<Severity, Long> severityCounts = findings.stream()
            .collect(Collectors.groupingBy(SecurityFinding::getSeverity, Collectors.counting()));
        
        Map<RemediationStatus, Long> statusCounts = remediationItems.stream()
            .collect(Collectors.groupingBy(RemediationItem::getRemediationStatus, Collectors.counting()));
        
        Map<String, Object> summary = new java.util.HashMap<>();
        summary.put("serviceName", serviceName);
        summary.put("totalFindings", findings.size());
        summary.put("openFindings", findings.stream().filter(f -> f.getStatus() == Status.OPEN).count());
        summary.put("priorityBreakdown", priorityCounts);
        summary.put("severityBreakdown", severityCounts);
        summary.put("remediationItems", remediationItems.size());
        summary.put("remediationStatusBreakdown", statusCounts);
        latestScan.ifPresent(scan -> summary.put("latestScan", scan));
        summary.put("p0Count", priorityCounts.getOrDefault(Priority.P0, 0L));
        summary.put("p1Count", priorityCounts.getOrDefault(Priority.P1, 0L));
        summary.put("p2Count", priorityCounts.getOrDefault(Priority.P2, 0L));
        summary.put("criticalCount", severityCounts.getOrDefault(Severity.CRITICAL, 0L));
        summary.put("highCount", severityCounts.getOrDefault(Severity.HIGH, 0L));
        
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/{serviceName}/security-report")
    public ResponseEntity<Map<String, Object>> getServiceSecurityReport(@PathVariable String serviceName) {
        // Get detailed security report for a service
        List<SecurityFinding> findings = securityFindingRepository.findByServiceName(serviceName);
        List<RemediationItem> remediationItems = remediationItemRepository.findByServiceName(serviceName);
        
        // Get service details
        ServiceDto service = serviceManagementService.getServiceByName(serviceName);
        
        Map<String, Object> report = Map.of(
            "service", service,
            "findings", findings,
            "remediationItems", remediationItems,
            "generatedAt", java.time.LocalDateTime.now().toString()
        );
        
        return ResponseEntity.ok(report);
    }

    @GetMapping("/{serviceName}/security-report/csv")
    public ResponseEntity<String> getServiceSecurityReportCsv(@PathVariable String serviceName) {
        List<SecurityFinding> findings = securityFindingRepository.findByServiceName(serviceName);
        List<RemediationItem> remediationItems = remediationItemRepository.findByServiceName(serviceName);
        List<ScanExecution> scans = scanExecutionRepository.findByServiceNameOrderByCreatedAtDesc(serviceName);
        List<ScanReport> sourceReports = scanReportRepository.findByServiceName(serviceName);

        StringBuilder csv = new StringBuilder();
        csv.append("Record Type,Service,Owner/Team,Environment,Priority,Severity,CVE,Title,Package,Installed Version,Fixed Version,Scanner,Scan Type,Status,Detection State,Risk Score,Scan Date,Source Reports\n");
        for (SecurityFinding finding : findings) {
            csv.append(csvRow("Finding", serviceName, "", finding.getEnvironment(), finding.getPriority(), finding.getSeverity(),
                finding.getCve(), finding.getTitle(), finding.getPackageName(), finding.getInstalledVersion(), finding.getFixedVersion(),
                finding.getTool(), finding.getScanType(), finding.getStatus(), finding.getDetectionState(), finding.getRiskScore(), finding.getLastDetectedAt(),
                finding.getSourceFindings()));
        }
        for (RemediationItem item : remediationItems) {
            csv.append(csvRow("Remediation", serviceName, item.getTeamName(), "", item.getPriority(), "", item.getFindingId(),
                item.getRecommendedAction(), "", "", "", "", "", item.getRemediationStatus(), "", item.getRiskScore(), item.getUpdatedAt()));
        }
        for (ScanExecution scan : scans) {
            csv.append(csvRow("Scan", serviceName, "", scan.getEnvironment(), "", "", "",
                scan.getNewFindings() + " new / " + scan.getResolvedFindings() + " resolved", "", "", "", scan.getTool(), scan.getScanType(),
                scan.getStatus(), "", scan.getTotalUniqueFindings(), scan.getCompletedAt() != null ? scan.getCompletedAt() : scan.getReceivedAt()));
        }
            for (ScanReport report : sourceReports) {
                csv.append(csvRow("Source Report", serviceName, "", report.getEnvironment(), "", "", report.getId(), report.getUploadedFileName(),
                "", "", "", report.getTool(), report.getScanType(), report.getStatus(), "", report.getTotalFindings(), report.getCreatedAt()));
            }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", serviceName + "-security-report.csv");
        return ResponseEntity.ok()
            .headers(headers)
            .body(csv.toString());
    }

    @PostMapping("/{serviceName}/security-report/email")
    public ResponseEntity<Map<String, String>> emailServiceSecurityReport(@PathVariable String serviceName) {
        notificationService.sendServiceReport(serviceName);
        return ResponseEntity.ok(Map.of("message", "Service report sent to the configured owner"));
    }

    private String csvRow(Object... values) {
        return java.util.Arrays.stream(values)
            .map(value -> "\"" + String.valueOf(value == null ? "" : value).replace("\"", "\"\"") + "\"")
            .collect(Collectors.joining(",")) + "\n";
    }
}