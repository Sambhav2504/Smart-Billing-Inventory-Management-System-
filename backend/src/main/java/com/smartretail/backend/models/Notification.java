package com.smartretail.backend.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Setter
@Getter
@Document(collection = "notifications")
public class Notification {
    @Id
    private String id;
    private String to;       // Email address
    private String subject;
    private String message;
    private Date sentAt;

    // Default constructor
    public Notification() {}

    // Constructor
    public Notification(String to, String subject, String message, Date sentAt) {
        this.to = to;
        this.subject = subject;
        this.message = message;
        this.sentAt = sentAt;
    }
}
