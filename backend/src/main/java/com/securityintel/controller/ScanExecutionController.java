package com.securityintel.controller;

import com.securityintel.exception.DatabaseException;
import com.securityintel.exception.ResourceNotFoundException;
import com.securityintel.model.ScanExecution;
import com.securityintel.scan.ScanExecutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/scans")
@CrossOrigin(origins = "*")
public class ScanExecutionController {

    private final ScanExecutionService scanExecutionService;

    public ScanExecutionController(ScanExecutionService scanExecutionService) {
        this.scanExecutionService = scanExecutionService;
    }

    @GetMapping
    public ResponseEntity<List<ScanExecution>> getAllScanExecutions() {
        try {
            List<ScanExecution> scans = scanExecutionService.getAllScanExecutions();
            return ResponseEntity.ok(scans);
        } catch (DatabaseException e) {
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScanExecution> getScanExecutionById(@PathVariable String id) {
        try {
            ScanExecution scan = scanExecutionService.getScanExecutionById(id);
            return ResponseEntity.ok(scan);
        } catch (ResourceNotFoundException e) {
            throw e;
        }
    }

    @GetMapping("/service/{serviceName}")
    public ResponseEntity<List<ScanExecution>> getScanExecutionsByService(@PathVariable String serviceName) {
        try {
            List<ScanExecution> scans = scanExecutionService.getScanExecutionsByService(serviceName);
            return ResponseEntity.ok(scans);
        } catch (DatabaseException e) {
            throw e;
        }
    }

    @GetMapping("/recent")
    public ResponseEntity<List<ScanExecution>> getRecentScanExecutions(
            @RequestParam(value = "hours", defaultValue = "24") int hours) {
        try {
            List<ScanExecution> scans = scanExecutionService.getRecentScanExecutions(hours);
            return ResponseEntity.ok(scans);
        } catch (DatabaseException e) {
            throw e;
        }
    }

    @GetMapping("/stale-summary")
    public ResponseEntity<ScanExecutionService.StaleServicesSummary> getStaleServicesSummary() {
        try {
            ScanExecutionService.StaleServicesSummary summary = scanExecutionService.getStaleServicesSummary();
            return ResponseEntity.ok(summary);
        } catch (DatabaseException e) {
            throw e;
        }
    }
}