package com.smartretail.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    private String productId;

    @NotBlank(message = "Name is required")
    private String name;

    private String category;

    @Positive(message = "Price must be positive")
    private double price;

    @Positive(message = "Quantity must be positive")
    private int quantity;

    private String expiryDate;
    private String imageUrl;
    private String supplierEmail;

    // Optional fields with defaults
    private int minQuantity = 5;
    private int reorderLevel = 10;
}