package com.securityintel.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "audit_events")
public class AuditEvent {
    @Id
    private String id;

    @Indexed
    private LocalDateTime occurredAt;

    @Indexed
    private String actor;

    @Indexed
    private String workspaceId;

    private String method;
    private String path;
    private int status;

    public AuditEvent() {
        this.occurredAt = LocalDateTime.now();
    }

    public AuditEvent(String actor, String workspaceId, String method, String path, int status) {
        this();
        this.actor = actor;
        this.workspaceId = workspaceId;
        this.method = method;
        this.path = path;
        this.status = status;
    }

    public String getId() { return id; }
    public String getActor() { return actor; }
    public String getWorkspaceId() { return workspaceId; }
    public String getMethod() { return method; }
    public String getPath() { return path; }
    public int getStatus() { return status; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
}
