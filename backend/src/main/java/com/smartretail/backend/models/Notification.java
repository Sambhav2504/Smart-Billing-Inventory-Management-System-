package com.smartretail.backend.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Data
@Document(collection = "notifications")
public class Notification {
    @Id
    private String id; // Maps to _id
    private String type; // "EMAIL", "SMS", "WHATSAPP"
    private String to; // Recipient address/number
    private String subject; // Nullable
    private String body;
    private String status; // "SENT", "FAILED"
    @Field("relatedTo")
    private RelatedTo relatedTo;
    private Date sentAt;

    @Data
    public static class RelatedTo {
        private String type; // e.g., "BILL"
        private String id; // References _id of related document
    }
}