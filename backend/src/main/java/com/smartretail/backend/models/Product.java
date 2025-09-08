package com.smartretail.backend.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Data
@Document(collection = "products")
public class Product {
    @Id
    private String id; // Maps to _id
    @Indexed(unique = true)
    private String productId; // Unique 4-digit ID (e.g., "1001")
    private String name;
    private String category;
    private double price;
    private int quantity;
    private int minQuantity;
    private Date expiryDate; // Optional (nullable)
    private String imageUrl; // Optional (nullable)
    @Field("addedBy") // Maps to addedBy (ObjectId in MongoDB)
    private String addedBy; // References users._id
    private Date lastUpdated;
}