package com.securityintel.mapper;

import com.securityintel.dto.*;
import com.securityintel.model.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EntityMapper {

    // Service mapping
    public ServiceDto toDto(Service service) {
        if (service == null) return null;

        ServiceDto dto = new ServiceDto();
        dto.setId(service.getId());
        dto.setServiceName(service.getServiceName());
        dto.setTeamName(service.getTeamName());
        dto.setEnvironment(service.getEnvironment());
        dto.setBusinessCriticality(service.getBusinessCriticality());
        dto.setInternetExposed(service.isInternetExposed());
        dto.setDataSensitivity(service.getDataSensitivity());
        dto.setRepository(service.getRepository());
        dto.setDeploymentPlatform(service.getDeploymentPlatform());
        dto.setOwner(service.getOwner());
        dto.setCreatedAt(service.getCreatedAt());
        dto.setUpdatedAt(service.getUpdatedAt());
        return dto;
    }

    public Service toEntity(ServiceDto dto) {
        if (dto == null) return null;

        Service service = new Service();
        service.setId(dto.getId());
        service.setServiceName(dto.getServiceName());
        service.setTeamName(dto.getTeamName());
        service.setEnvironment(dto.getEnvironment());
        service.setBusinessCriticality(dto.getBusinessCriticality());
        service.setInternetExposed(dto.isInternetExposed());
        service.setDataSensitivity(dto.getDataSensitivity());
        service.setRepository(dto.getRepository());
        service.setDeploymentPlatform(dto.getDeploymentPlatform());
        service.setOwner(dto.getOwner());
        
        if (dto.getId() == null) {
            service.setCreatedAt(LocalDateTime.now());
        } else {
            service.setCreatedAt(dto.getCreatedAt());
        }
        service.setUpdatedAt(LocalDateTime.now());
        
        return service;
    }

    // SecurityFinding mapping
    public SecurityFindingDto toDto(SecurityFinding finding) {
        if (finding == null) return null;

        SecurityFindingDto dto = new SecurityFindingDto();
        dto.setId(finding.getId());
        dto.setReportId(finding.getReportId());
        dto.setTool(finding.getTool());
        dto.setScanType(finding.getScanType());
        dto.setServiceName(finding.getServiceName());
        dto.setRepository(finding.getRepository());
        dto.setEnvironment(finding.getEnvironment());
        dto.setSeverity(finding.getSeverity());
        dto.setCvssScore(finding.getCvssScore());
        dto.setCve(finding.getCve());
        dto.setCwe(finding.getCwe());
        dto.setTitle(finding.getTitle());
        dto.setDescription(finding.getDescription());
        dto.setPackageName(finding.getPackageName());
        dto.setInstalledVersion(finding.getInstalledVersion());
        dto.setFixedVersion(finding.getFixedVersion());
        dto.setFilePath(finding.getFilePath());
        dto.setLineNumber(finding.getLineNumber());
        dto.setContainerImage(finding.getContainerImage());
        dto.setEndpoint(finding.getEndpoint());
        dto.setHttpMethod(finding.getHttpMethod());
        dto.setStatus(finding.getStatus());
        dto.setFingerprint(finding.getFingerprint());
        dto.setSourceFindings(finding.getSourceFindings());
        dto.setRiskScore(finding.getRiskScore());
        dto.setPriority(finding.getPriority());
        dto.setPriorityReasons(finding.getPriorityReasons());
        dto.setCreatedAt(finding.getCreatedAt());
        dto.setUpdatedAt(finding.getUpdatedAt());
        return dto;
    }

    // ScanReport mapping
    public ScanReportDto toDto(ScanReport report) {
        if (report == null) return null;

        ScanReportDto dto = new ScanReportDto();
        dto.setId(report.getId());
        dto.setTool(report.getTool());
        dto.setScanType(report.getScanType());
        dto.setServiceName(report.getServiceName());
        dto.setRepository(report.getRepository());
        dto.setBranch(report.getBranch());
        dto.setCommitId(report.getCommitId());
        dto.setEnvironment(report.getEnvironment());
        dto.setUploadedFileName(report.getUploadedFileName());
        dto.setTotalFindings(report.getTotalFindings());
        dto.setStatus(report.getStatus());
        dto.setCreatedAt(report.getCreatedAt());
        return dto;
    }

    // List mapping utilities
    public List<ServiceDto> toServiceDtos(List<Service> services) {
        return services.stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<SecurityFindingDto> toFindingDtos(List<SecurityFinding> findings) {
        return findings.stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<ScanReportDto> toReportDtos(List<ScanReport> reports) {
        return reports.stream().map(this::toDto).collect(Collectors.toList());
    }

    // Top priority finding mapping
    public DashboardSummaryDto.TopPriorityFindingDto toTopPriorityDto(SecurityFinding finding) {
        if (finding == null) return null;

        return new DashboardSummaryDto.TopPriorityFindingDto(
            finding.getCve() != null ? finding.getCve() : "N/A",
            finding.getServiceName(),
            finding.getSeverity() != null ? finding.getSeverity().name() : "UNKNOWN",
            finding.getRiskScore(),
            finding.getPriority() != null ? finding.getPriority().name() : "P4"
        );
    }
}