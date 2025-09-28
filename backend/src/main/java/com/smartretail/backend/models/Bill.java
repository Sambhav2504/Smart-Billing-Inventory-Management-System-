package com.smartretail.backend.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@Document(collection = "bills")
public class Bill {
    @Id
    private String billId;
    private CustomerInfo customer;
    private List<BillItem> items;
    private double totalAmount;
    private Date createdAt;
    private String pdfAccessToken;
    private String addedBy;

    // Default constructor
    public Bill() {
        this.createdAt = new Date();
        this.pdfAccessToken = UUID.randomUUID().toString();
    }

    // Constructor with required fields
    public Bill(String billId, List<BillItem> items, String addedBy) {
        this();
        this.billId = billId;
        this.items = items;
        this.addedBy = addedBy;
    }

    // Full constructor
    public Bill(String billId, CustomerInfo customer, List<BillItem> items,
                double totalAmount, String addedBy) {
        this(billId, items, addedBy);
        this.customer = customer;
        this.totalAmount = totalAmount;
    }

    @Setter
    @Getter
    public static class BillItem {
        private String productId;
        private String productName;
        private int qty;
        private double price;

        // Default constructor
        public BillItem() {}

        // Constructor with fields
        public BillItem(String productId, String productName, int qty, double price) {
            this.productId = productId;
            this.productName = productName;
            this.qty = qty;
            this.price = price;
        }

        // Calculate item total
        public double getItemTotal() {
            return this.qty * this.price;
        }
    }

    @Setter
    @Getter
    public static class CustomerInfo {
        private String name;
        private String email;
        private String mobile;

        // Default constructor
        public CustomerInfo() {}

        // Constructor with fields
        public CustomerInfo(String name, String email, String mobile) {
            this.name = name;
            this.email = email;
            this.mobile = mobile;
        }

        // Validation method
        public boolean isValid() {
            return this.mobile != null && !this.mobile.trim().isEmpty();
        }
    }

    // Validation method for the entire bill
    public boolean isValid() {
        return this.billId != null &&
                !this.billId.trim().isEmpty() &&
                this.items != null &&
                !this.items.isEmpty() &&
                this.addedBy != null &&
                !this.addedBy.trim().isEmpty();
    }

    // Calculate total from items
    public double calculateTotal() {
        if (this.items == null || this.items.isEmpty()) {
            return 0.0;
        }
        return this.items.stream()
                .mapToDouble(BillItem::getItemTotal)
                .sum();
    }
}