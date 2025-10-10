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
    private String id; // MongoDB _id
    private String productId; // Your custom product ID
    private String name;
    private String category;
    private double price;
    private int quantity;
    private int minQuantity;
    private int reorderLevel;
    private Date expiryDate;
    private String imageId; // GridFS ID for image
    private String imageUrl; // URL for image access
    private String supplierEmail;
    private String addedBy;
    private Date lastUpdated;
    private Date createdAt;

    public Product() {
        this.createdAt = new Date();
        this.lastUpdated = new Date();
    }

    public Product(String productId, String name, String category, double price, int quantity,
                   int minQuantity, int reorderLevel, Date expiryDate, String imageId, String imageUrl,
                   String supplierEmail, String addedBy) {
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
        this.minQuantity = minQuantity;
        this.reorderLevel = reorderLevel;
        this.expiryDate = expiryDate;
        this.imageId = imageId;
        this.imageUrl = imageUrl;
        this.supplierEmail = supplierEmail;
        this.addedBy = addedBy;
        this.lastUpdated = new Date();
        this.createdAt = new Date();
    }
}