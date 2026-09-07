package com.securityintel.repository;

import com.securityintel.model.AuditEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AuditEventRepository extends MongoRepository<AuditEvent, String> {
    List<AuditEvent> findTop100ByOrderByOccurredAtDesc();
    List<AuditEvent> findTop100ByWorkspaceIdOrderByOccurredAtDesc(String workspaceId);
}
