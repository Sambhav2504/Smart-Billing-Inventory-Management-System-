package com.smartretail.backend.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;

@Data
@Document(collection = "bills")
public class Bill {
    @Id
    private String id; // Maps to _id
    @Indexed(unique = true)
    private String billId; // Human-readable ID (e.g., "inv_25081710")
    private Date date;
    private String customerMobile; // Nullable, references customers.mobile
    private List<Item> items; // Embedded items
    private double subTotal;
    private double taxRate;
    private double taxAmount;
    private double totalAmount;
    private String paymentMode; // "CASH", "UPI"
    private String paymentStatus; // "PENDING", "PAID", "FAILED"
    @Field("cashierId")
    private String cashierId; // References users._id

    @Data
    public static class Item {
        private String productId; // References products.productId
        private String name; // Snapshot of product name
        private int quantity;
        private double price; // Snapshot of price
    }
}