package com.smartretail.backend.models;

import jakarta.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "bills")
public class Bill {
    @Id
    private String id;

    private String billId; // Removed @NotBlank as it's set by service

    @NotEmpty(message = "Items list cannot be empty")
    @Size(min = 1, message = "At least one item is required")
    private List<BillItem> items;

    private double total; // Removed @Positive as it's calculated by service

    @NotNull(message = "Customer info is required")
    private CustomerInfo customer;

    private Date createdAt; // Removed @NotNull as it's set by service

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getBillId() { return billId; }
    public void setBillId(String billId) { this.billId = billId; }
    public List<BillItem> getItems() { return items; }
    public void setItems(List<BillItem> items) { this.items = items; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public CustomerInfo getCustomer() { return customer; }
    public void setCustomer(CustomerInfo customer) { this.customer = customer; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    // Nested BillItem class
    public static class BillItem {
        @NotBlank(message = "Product ID is required")
        private String productId;

        @Positive(message = "Quantity must be positive")
        private int qty;

        @Positive(message = "Price must be positive")
        private double price;

        // Constructor
        public BillItem() {}

        // Getters and Setters
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        public int getQty() { return qty; }
        public void setQty(int qty) { this.qty = qty; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
    }

    // Nested CustomerInfo class
    public static class CustomerInfo {
        @NotBlank(message = "Customer mobile is required")
        @Pattern(regexp = "\\d{10}", message = "Mobile must be a 10-digit number")
        private String mobile;

        private String name;
        private String email;

        // Constructor
        public CustomerInfo() {}

        // Getters and Setters
        public String getMobile() { return mobile; }
        public void setMobile(String mobile) { this.mobile = mobile; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}