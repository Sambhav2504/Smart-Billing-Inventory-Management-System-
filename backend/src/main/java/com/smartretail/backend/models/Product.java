package com.smartretail.backend.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Setter
@Getter
@Document(collection = "products")
public class Product {
    @Id
    private String productId;
    private String name;
    private String category;
    private double price;
    private int quantity;
    private int minQuantity;
    private int reorderLevel;
    private Date expiryDate;
    private String imageId; // GridFS ID for image
    private String addedBy;
    private Date lastUpdated;
    private Date createdAt;

    public Product() {
        this.createdAt = new Date();
    }

    public Product(String productId, String name, String category, double price, int quantity,
                   int minQuantity, int reorderLevel, Date expiryDate, String imageId, String addedBy) {
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
        this.minQuantity = minQuantity;
        this.reorderLevel = reorderLevel;
        this.expiryDate = expiryDate;
        this.imageId = imageId;
        this.addedBy = addedBy;
        this.lastUpdated = new Date();
        this.createdAt = new Date();
    }

    // Helper method to get ID for compatibility
    public String getId() {
        return this.productId;
    }

    // Helper method to set ID for compatibility
    public void setId(String id) {
        this.productId = id;
    }
}