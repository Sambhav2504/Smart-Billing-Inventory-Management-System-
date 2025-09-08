package com.smartretail.backend.dto;

public class ProductRequest {
    private String id; // This will be a unique identifier we generate
    private String productId; // The 4-digit ID from the user
    private String name;
    private String category;
    private double price;
    private int quantity;

    // Constructors
    public ProductRequest() {}

    public ProductRequest(String id, String productId, String name, String category, double price, int quantity) {
        this.id = id;
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
    }

    // Generate Getters and Setters for ALL fields
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}