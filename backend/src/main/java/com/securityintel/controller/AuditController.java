package com.securityintel.controller;

import com.securityintel.model.AuditEvent;
import com.securityintel.repository.AuditEventRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/audit")
@CrossOrigin(origins = "*")
public class AuditController {
    private final AuditEventRepository auditEventRepository;

    public AuditController(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @GetMapping
    public ResponseEntity<List<AuditEvent>> getRecentAuditEvents() {
        return ResponseEntity.ok(auditEventRepository.findTop100ByOrderByOccurredAtDesc());
    }
}
