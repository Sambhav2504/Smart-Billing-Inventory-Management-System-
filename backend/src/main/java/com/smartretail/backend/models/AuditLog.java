package com.smartretail.backend.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Map;

@Setter
@Getter
@Document(collection = "auditLogs")
public class AuditLog {
    // Getters and Setters
    @Id
    private String id;
    private String actionType;
    private String entityId;
    private String userEmail;
    private Date timestamp;
    private Map<String, Object> details;

    // Constructors
    public AuditLog() {}

    public AuditLog(String actionType, String entityId, String userEmail, Date timestamp, Map<String, Object> details) {
        this.actionType = actionType;
        this.entityId = entityId;
        this.userEmail = userEmail;
        this.timestamp = timestamp;
        this.details = details;
    }

}