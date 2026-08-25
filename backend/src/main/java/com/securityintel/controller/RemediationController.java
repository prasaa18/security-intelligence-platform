package com.securityintel.controller;

import com.securityintel.exception.DatabaseException;
import com.securityintel.exception.ResourceNotFoundException;
import com.securityintel.model.Priority;
import com.securityintel.model.RemediationItem;
import com.securityintel.model.RemediationStatus;
import com.securityintel.remediation.RemediationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/remediation")
@CrossOrigin(origins = "*")
public class RemediationController {

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
            RemediationItem item = remediationService.updateRemediationStatus(id, request.getRemediationStatus());
            return ResponseEntity.ok(item);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
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
}