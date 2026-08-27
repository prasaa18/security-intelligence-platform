package com.securityintel.controller;

import com.securityintel.dto.SecurityFindingDto;
import com.securityintel.model.Priority;
import com.securityintel.model.Severity;
import com.securityintel.service.SecurityFindingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/findings")
@CrossOrigin(origins = "*")
public class FindingController {

    private final SecurityFindingService securityFindingService;

    public FindingController(SecurityFindingService securityFindingService) {
        this.securityFindingService = securityFindingService;
    }

    @GetMapping
    public ResponseEntity<List<SecurityFindingDto>> getAllFindings() {
        List<SecurityFindingDto> findings = securityFindingService.getAllFindings();
        return ResponseEntity.ok(findings);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SecurityFindingDto> getFindingById(@PathVariable String id) {
        SecurityFindingDto finding = securityFindingService.getFindingById(id);
        return ResponseEntity.ok(finding);
    }

    @GetMapping("/service/{serviceName}")
    public ResponseEntity<List<SecurityFindingDto>> getFindingsByService(@PathVariable String serviceName) {
        List<SecurityFindingDto> findings = securityFindingService.getFindingsByService(serviceName);
        return ResponseEntity.ok(findings);
    }

    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<SecurityFindingDto>> getFindingsByPriority(@PathVariable Priority priority) {
        List<SecurityFindingDto> findings = securityFindingService.getFindingsByPriority(priority);
        return ResponseEntity.ok(findings);
    }

    @GetMapping("/severity/{severity}")
    public ResponseEntity<List<SecurityFindingDto>> getFindingsBySeverity(@PathVariable Severity severity) {
        List<SecurityFindingDto> findings = securityFindingService.getFindingsBySeverity(severity);
        return ResponseEntity.ok(findings);
    }

    @GetMapping("/prioritized")
    public ResponseEntity<List<SecurityFindingDto>> getTopPriorityFindings(@RequestParam(defaultValue = "10") int limit) {
        List<SecurityFindingDto> findings = securityFindingService.getTopPriorityFindings(limit);
        return ResponseEntity.ok(findings);
    }

    @GetMapping("/search")
    public ResponseEntity<List<SecurityFindingDto>> searchFindings(@RequestParam String query) {
        List<SecurityFindingDto> findings = securityFindingService.searchFindings(query);
        return ResponseEntity.ok(findings);
    }

    @GetMapping("/export/csv")
    public ResponseEntity<String> exportAllFindingsCsv() {
        String csv = securityFindingService.exportAllFindingsCsv();
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=security-findings.csv")
                .body(csv);
    }
}