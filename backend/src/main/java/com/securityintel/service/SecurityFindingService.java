package com.securityintel.service;

import com.securityintel.dto.SecurityFindingDto;
import com.securityintel.exception.DatabaseException;
import com.securityintel.exception.ResourceNotFoundException;
import com.securityintel.mapper.EntityMapper;
import com.securityintel.model.*;
import com.securityintel.repository.SecurityFindingRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SecurityFindingService {

    private final SecurityFindingRepository securityFindingRepository;
    private final EntityMapper entityMapper;

    public SecurityFindingService(SecurityFindingRepository securityFindingRepository, EntityMapper entityMapper) {
        this.securityFindingRepository = securityFindingRepository;
        this.entityMapper = entityMapper;
    }

    public List<SecurityFindingDto> getAllFindings() {
        try {
            List<SecurityFinding> findings = securityFindingRepository.findAll();
            return entityMapper.toFindingDtos(findings);
        } catch (Exception e) {
            throw new DatabaseException("Failed to retrieve findings", e);
        }
    }

    public SecurityFindingDto getFindingById(String id) {
        SecurityFinding finding = securityFindingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Finding not found with id: " + id));
        return entityMapper.toDto(finding);
    }

    public List<SecurityFindingDto> getFindingsByService(String serviceName) {
        try {
            List<SecurityFinding> findings = securityFindingRepository.findByServiceName(serviceName);
            return entityMapper.toFindingDtos(findings);
        } catch (Exception e) {
            throw new DatabaseException("Failed to retrieve findings for service: " + serviceName, e);
        }
    }

    public List<SecurityFindingDto> getFindingsByPriority(Priority priority) {
        try {
            List<SecurityFinding> findings = securityFindingRepository.findByPriorityAndStatus(priority, Status.OPEN);
            return entityMapper.toFindingDtos(findings);
        } catch (Exception e) {
            throw new DatabaseException("Failed to retrieve findings by priority: " + priority, e);
        }
    }

    public List<SecurityFindingDto> getFindingsBySeverity(Severity severity) {
        try {
            List<SecurityFinding> findings = securityFindingRepository.findBySeverityAndStatus(severity, Status.OPEN);
            return entityMapper.toFindingDtos(findings);
        } catch (Exception e) {
            throw new DatabaseException("Failed to retrieve findings by severity: " + severity, e);
        }
    }

    public List<SecurityFindingDto> getTopPriorityFindings(int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            List<SecurityFinding> findings = securityFindingRepository.findTopPriorityFindings(pageable);
            return entityMapper.toFindingDtos(findings);
        } catch (Exception e) {
            throw new DatabaseException("Failed to retrieve top priority findings", e);
        }
    }

    public List<SecurityFindingDto> searchFindings(String query) {
        try {
            // Simple search by CVE or title - can be enhanced
            List<SecurityFinding> findings = securityFindingRepository.findByCve(query);
            if (findings.isEmpty()) {
                // If no CVE match, search could be extended to other fields
                findings = securityFindingRepository.findAll().stream()
                    .filter(f -> (f.getTitle() != null && f.getTitle().toLowerCase().contains(query.toLowerCase())) ||
                                (f.getCve() != null && f.getCve().toLowerCase().contains(query.toLowerCase())))
                    .toList();
            }
            return entityMapper.toFindingDtos(findings);
        } catch (Exception e) {
            throw new DatabaseException("Failed to search findings", e);
        }
    }

    public String exportAllFindingsCsv() {
        try {
            List<SecurityFinding> findings = securityFindingRepository.findAll();
            List<SecurityFindingDto> findingDtos = entityMapper.toFindingDtos(findings);
            
            StringBuilder csv = new StringBuilder();
            csv.append("CVE,Title,Service,Environment,Tool,Severity,CVSS,Risk Score,Priority,Status,Package,Installed Version,Fixed Version,First Detected At,Last Detected At\n");
            
            for (SecurityFindingDto finding : findingDtos) {
                csv.append(finding.getCve() != null ? finding.getCve() : "").append(",");
                csv.append(finding.getTitle() != null ? ("\"" + finding.getTitle().replace("\"", "\"\"") + "\"") : "").append(",");
                csv.append(finding.getServiceName() != null ? finding.getServiceName() : "").append(",");
                csv.append(finding.getEnvironment() != null ? finding.getEnvironment() : "").append(",");
                csv.append(finding.getTool() != null ? finding.getTool() : "").append(",");
                csv.append(finding.getSeverity() != null ? finding.getSeverity() : "").append(",");
                csv.append(finding.getCvssScore() != null ? finding.getCvssScore() : "").append(",");
                csv.append(finding.getRiskScore() != null ? finding.getRiskScore() : "").append(",");
                csv.append(finding.getPriority() != null ? finding.getPriority() : "").append(",");
                csv.append(finding.getStatus() != null ? finding.getStatus() : "").append(",");
                csv.append(finding.getPackageName() != null ? finding.getPackageName() : "").append(",");
                csv.append(finding.getInstalledVersion() != null ? finding.getInstalledVersion() : "").append(",");
                csv.append(finding.getFixedVersion() != null ? finding.getFixedVersion() : "").append(",");
                csv.append(finding.getFirstDetectedAt() != null ? finding.getFirstDetectedAt() : "").append(",");
                csv.append(finding.getLastDetectedAt() != null ? finding.getLastDetectedAt() : "").append("\n");
            }
            
            return csv.toString();
        } catch (Exception e) {
            throw new DatabaseException("Failed to export findings as CSV", e);
        }
    }
}