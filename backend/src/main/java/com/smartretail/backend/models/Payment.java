package com.smartretail.backend.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Data
@Document(collection = "payments")
public class Payment {
    @Id
    private String id; // Maps to _id
    @Indexed(unique = true)
    private String paymentId; // e.g., "pay_2508171011"
    @Field("billId")
    private String billId; // References bills._id
    private double amount;
    private String paymentMode; // "CASH", "UPI"
    private String status; // "INITIATED", "SUCCESS", "FAILED"
    private String upiTransactionId; // Nullable
    private Date createdAt;
}