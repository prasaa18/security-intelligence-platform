package com.securityintel.remediation;

import com.securityintel.exception.DatabaseException;
import com.securityintel.exception.ResourceNotFoundException;
import com.securityintel.model.Priority;
import com.securityintel.model.RemediationItem;
import com.securityintel.model.RemediationStatus;
import com.securityintel.model.SecurityFinding;
import com.securityintel.model.Service;
import com.securityintel.repository.RemediationItemRepository;
import com.securityintel.repository.SecurityFindingRepository;
import com.securityintel.repository.ServiceRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class RemediationService {

    private final RemediationItemRepository remediationItemRepository;
    private final SecurityFindingRepository securityFindingRepository;
    private final ServiceRepository serviceRepository;

    public RemediationService(RemediationItemRepository remediationItemRepository,
                              SecurityFindingRepository securityFindingRepository,
                              ServiceRepository serviceRepository) {
        this.remediationItemRepository = remediationItemRepository;
        this.securityFindingRepository = securityFindingRepository;
        this.serviceRepository = serviceRepository;
    }

    public RemediationItem createOrUpdateRemediationItem(SecurityFinding finding) {
        Optional<RemediationItem> existingItem = remediationItemRepository.findByFindingId(finding.getId());
        
        RemediationItem item;
        if (existingItem.isPresent()) {
            item = existingItem.get();
            item.setLastDetectedAt(LocalDateTime.now());
            item.setUpdatedAt(LocalDateTime.now());
            item.setPriority(finding.getPriority());
            item.setRiskScore(finding.getRiskScore());
            
            // If the finding is still open, ensure remediation status is not RESOLVED
            if (finding.getStatus() == com.securityintel.model.Status.OPEN && 
                item.getRemediationStatus() == RemediationStatus.RESOLVED) {
                item.setRemediationStatus(RemediationStatus.OPEN);
            }
        } else {
            item = new RemediationItem();
            item.setFindingId(finding.getId());
            item.setServiceName(finding.getServiceName());
            item.setPriority(finding.getPriority());
            item.setRiskScore(finding.getRiskScore());
            item.setFirstDetectedAt(finding.getFirstDetectedAt() != null ? finding.getFirstDetectedAt() : LocalDateTime.now());
            item.setLastDetectedAt(LocalDateTime.now());
            item.setLatestScanAt(finding.getUpdatedAt());
            
            // Set team name from service
            Optional<Service> service = serviceRepository.findByServiceName(finding.getServiceName());
            service.ifPresent(s -> item.setTeamName(s.getTeamName()));
            
            // Generate recommended action
            item.setRecommendedAction(generateRecommendedAction(finding));
        }
        
        return remediationItemRepository.save(item);
    }

    public List<RemediationItem> getRemediationItems() {
        try {
            return remediationItemRepository.findAll();
        } catch (Exception e) {
            throw new DatabaseException("Failed to retrieve remediation items", e);
        }
    }

    public RemediationItem getRemediationItemById(String id) {
        return remediationItemRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Remediation item not found with id: " + id));
    }

    public List<RemediationItem> getRemediationItemsByService(String serviceName) {
        try {
            return remediationItemRepository.findByServiceNameOrderByPriorityDesc(serviceName);
        } catch (Exception e) {
            throw new DatabaseException("Failed to retrieve remediation items for service: " + serviceName, e);
        }
    }

    public List<RemediationItem> getRemediationItemsByPriority(Priority priority) {
        try {
            return remediationItemRepository.findByPriorityOrderByRiskScoreDesc(priority);
        } catch (Exception e) {
            throw new DatabaseException("Failed to retrieve remediation items by priority", e);
        }
    }

    public List<RemediationItem> getRemediationItemsByTeam(String teamName) {
        try {
            return remediationItemRepository.findByTeamName(teamName);
        } catch (Exception e) {
            throw new DatabaseException("Failed to retrieve remediation items for team: " + teamName, e);
        }
    }

    public RemediationItem updateRemediationStatus(String id, RemediationStatus newStatus) {
        RemediationItem item = getRemediationItemById(id);
        
        // Validate status transitions
        if (!isValidStatusTransition(item.getRemediationStatus(), newStatus)) {
            throw new IllegalArgumentException(
                "Invalid status transition from " + item.getRemediationStatus() + " to " + newStatus);
        }
        
        item.setRemediationStatus(newStatus);
        item.setUpdatedAt(LocalDateTime.now());
        
        if (newStatus == RemediationStatus.RESOLVED) {
            item.setResolvedAt(LocalDateTime.now());
            // Also update the associated finding status
            Optional<SecurityFinding> finding = securityFindingRepository.findById(item.getFindingId());
            finding.ifPresent(f -> {
                f.setStatus(com.securityintel.model.Status.RESOLVED);
                f.setUpdatedAt(LocalDateTime.now());
                securityFindingRepository.save(f);
            });
        }
        
        return remediationItemRepository.save(item);
    }

    public ActionCenterSummary getActionCenterSummary() {
        try {
            long immediateActions = remediationItemRepository.countByPriorityAndStatus(
                Priority.P0, com.securityintel.model.Status.OPEN);
            
            long dueThisWeek = remediationItemRepository.countByPriorityAndStatus(
                Priority.P1, com.securityintel.model.Status.OPEN);
            
            long recentlyResolved = remediationItemRepository.countByRemediationStatus(
                RemediationStatus.RESOLVED);
            
            return new ActionCenterSummary(immediateActions, dueThisWeek, recentlyResolved);
        } catch (Exception e) {
            throw new DatabaseException("Failed to generate action center summary", e);
        }
    }

    public List<RemediationItem> getTopRemediationItems(int limit) {
        try {
            org.springframework.data.domain.Pageable pageable = 
                org.springframework.data.domain.PageRequest.of(0, limit);
            return remediationItemRepository.findTopOpenRemediationItems(pageable);
        } catch (Exception e) {
            throw new DatabaseException("Failed to retrieve top remediation items", e);
        }
    }

    private String generateRecommendedAction(SecurityFinding finding) {
        StringBuilder action = new StringBuilder();
        
        if (finding.getFixedVersion() != null && !finding.getFixedVersion().isEmpty()) {
            if (finding.getPackageName() != null) {
                action.append("Upgrade ").append(finding.getPackageName());
                action.append(" from ").append(finding.getInstalledVersion());
                action.append(" to ").append(finding.getFixedVersion()).append(".");
            } else {
                action.append("Update to fixed version: ").append(finding.getFixedVersion()).append(".");
            }
        } else {
            action.append("No fixed version available from the scanner.");
            if (finding.getTitle() != null) {
                action.append(" Review: ").append(finding.getTitle());
            }
        }
        
        return action.toString();
    }

    private boolean isValidStatusTransition(RemediationStatus current, RemediationStatus newStatus) {
        return switch (newStatus) {
            case OPEN -> current == RemediationStatus.NEW || current == RemediationStatus.IN_PROGRESS;
            case IN_PROGRESS -> current == RemediationStatus.NEW || current == RemediationStatus.OPEN;
            case RESOLVED -> current == RemediationStatus.OPEN || current == RemediationStatus.IN_PROGRESS;
            case ACCEPTED_RISK -> current == RemediationStatus.OPEN || current == RemediationStatus.IN_PROGRESS;
            default -> false;
        };
    }

    public static class ActionCenterSummary {
        private final long immediateActions;
        private final long dueThisWeek;
        private final long recentlyResolved;

        public ActionCenterSummary(long immediateActions, long dueThisWeek, long recentlyResolved) {
            this.immediateActions = immediateActions;
            this.dueThisWeek = dueThisWeek;
            this.recentlyResolved = recentlyResolved;
        }

        public long getImmediateActions() {
            return immediateActions;
        }

        public long getDueThisWeek() {
            return dueThisWeek;
        }

        public long getRecentlyResolved() {
            return recentlyResolved;
        }
    }
}