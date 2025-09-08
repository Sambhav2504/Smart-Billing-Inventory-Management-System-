package com.smartretail.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data // Lombok: Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok: Generates the default constructor ProductRequest()
@AllArgsConstructor // Lombok: Generates constructor with all fields
public class ProductRequest {
    private String id;
    private String productId;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Size(max = 100, message = "Category cannot exceed 100 characters")
    private String category;

    @Positive(message = "Price must be positive")
    private double price;

    @Positive(message = "Quantity must be positive")
    private int quantity;
}