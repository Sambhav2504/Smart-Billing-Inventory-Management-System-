package com.smartretail.backend.models;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Setter
@Getter
@Document(collection = "customers")
public class Customer {
    // Getters and Setters
    @Id
    private String id;

    @NotBlank(message = "Customer name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "\\d{10}", message = "Mobile must be 10 digits")
    private String mobile;

    @NotNull(message = "Created date is required")
    private Date createdAt;

    private List<String> purchaseHistory = new ArrayList<>(); // List of billIds

    // Constructors
    public Customer() {}

    public Customer(String name, String email, String mobile, Date createdAt) {
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.createdAt = createdAt;
        this.purchaseHistory = new ArrayList<>();
    }

    public void addBillId(String billId) { this.purchaseHistory.add(billId); }
}