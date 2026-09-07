package com.securityintel.controller;

import com.securityintel.exception.DatabaseException;
import com.securityintel.exception.ResourceNotFoundException;
import com.securityintel.model.Priority;
import com.securityintel.model.RemediationItem;
import com.securityintel.model.RemediationStatus;
import com.securityintel.remediation.RemediationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/remediation")
@CrossOrigin(origins = "*")
public class RemediationController {

    private static final Logger log = LoggerFactory.getLogger(RemediationController.class);

    private final RemediationService remediationService;

    public RemediationController(RemediationService remediationService) {
        this.remediationService = remediationService;
    }

    @GetMapping
    public ResponseEntity<List<RemediationItem>> getAllRemediationItems() {
        try {
            List<RemediationItem> items = remediationService.getRemediationItems();
            return ResponseEntity.ok(items);
        } catch (DatabaseException e) {
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<RemediationItem> getRemediationItemById(@PathVariable String id) {
        try {
            RemediationItem item = remediationService.getRemediationItemById(id);
            return ResponseEntity.ok(item);
        } catch (ResourceNotFoundException e) {
            throw e;
        }
    }

    @GetMapping("/service/{serviceName}")
    public ResponseEntity<List<RemediationItem>> getRemediationItemsByService(@PathVariable String serviceName) {
        try {
            List<RemediationItem> items = remediationService.getRemediationItemsByService(serviceName);
            return ResponseEntity.ok(items);
        } catch (DatabaseException e) {
            throw e;
        }
    }

    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<RemediationItem>> getRemediationItemsByPriority(@PathVariable Priority priority) {
        try {
            List<RemediationItem> items = remediationService.getRemediationItemsByPriority(priority);
            return ResponseEntity.ok(items);
        } catch (DatabaseException e) {
            throw e;
        }
    }

    @GetMapping("/team/{teamName}")
    public ResponseEntity<List<RemediationItem>> getRemediationItemsByTeam(@PathVariable String teamName) {
        try {
            List<RemediationItem> items = remediationService.getRemediationItemsByTeam(teamName);
            return ResponseEntity.ok(items);
        } catch (DatabaseException e) {
            throw e;
        }
    }

    @GetMapping("/action-center")
    public ResponseEntity<RemediationService.ActionCenterSummary> getActionCenterSummary() {
        try {
            RemediationService.ActionCenterSummary summary = remediationService.getActionCenterSummary();
            return ResponseEntity.ok(summary);
        } catch (DatabaseException e) {
            throw e;
        }
    }

    @GetMapping("/top")
    public ResponseEntity<List<RemediationItem>> getTopRemediationItems(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        try {
            List<RemediationItem> items = remediationService.getTopRemediationItems(limit);
            return ResponseEntity.ok(items);
        } catch (DatabaseException e) {
            throw e;
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<RemediationItem> updateRemediationStatus(
            @PathVariable String id,
            @RequestBody RemediationStatusUpdateRequest request) {
        try {
            log.info("Updating remediation status for id: {} to status: {}", id, request.getRemediationStatus());
            RemediationItem item = remediationService.updateRemediationStatus(id, request.getRemediationStatus());
            log.info("Successfully updated remediation status for id: {}", id);
            return ResponseEntity.ok(item);
        } catch (ResourceNotFoundException e) {
            log.error("Remediation item not found with id: {}", id);
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("Invalid status transition for remediation item {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            log.error("Failed to update status for remediation item {}: {} - {}", id, e.getMessage(), e.getClass().getSimpleName(), e);
            throw new RuntimeException("Failed to update status for remediation item: " + e.getMessage(), e);
        }
    }

    @PutMapping("/batch-status")
    public ResponseEntity<List<RemediationItem>> batchUpdateRemediationStatus(
            @RequestBody BatchRemediationStatusRequest request) {
        try {
            if (request == null || request.getIds() == null || request.getRemediationStatus() == null) {
                return ResponseEntity.badRequest().build();
            }
            List<RemediationItem> updated = remediationService.batchUpdateStatus(request.getIds(), request.getRemediationStatus());
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Failed batch update: {}", e.getMessage(), e);
            throw new RuntimeException("Batch update failed: " + e.getMessage(), e);
        }
    }

    public static class RemediationStatusUpdateRequest {
        private RemediationStatus remediationStatus;

        public RemediationStatus getRemediationStatus() {
            return remediationStatus;
        }

        public void setRemediationStatus(RemediationStatus remediationStatus) {
            this.remediationStatus = remediationStatus;
        }
    }

    public static class BatchRemediationStatusRequest {
        private List<String> ids;
        private RemediationStatus remediationStatus;

        public List<String> getIds() {
            return ids;
        }

        public void setIds(List<String> ids) {
            this.ids = ids;
        }

        public RemediationStatus getRemediationStatus() {
            return remediationStatus;
        }

        public void setRemediationStatus(RemediationStatus remediationStatus) {
            this.remediationStatus = remediationStatus;
        }
    }
}