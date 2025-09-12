package com.smartretail.backend.models;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Setter
@Getter
@Document(collection = "bills")
public class Bill {
    @Id
    private String id; // MongoDB _id (ObjectId or UUID)

    @NotBlank(message = "Bill ID is required")
    private String billId; // custom human-readable bill id (b1234 etc.)

    @NotEmpty(message = "Items list cannot be empty")
    @Size(min = 1, message = "At least one item is required")
    private List<BillItem> items;

    private double total;

    @NotNull(message = "Customer info is required")
    private CustomerInfo customer;

    private Date createdAt;

    @Setter
    @Getter
    public static class BillItem {
        @NotBlank(message = "Product ID is required")
        private String productId;

        @Positive(message = "Quantity must be positive")
        private int qty;

        @Positive(message = "Price must be positive")
        private double price;

        public BillItem() {}
    }

    @Setter
    @Getter
    public static class CustomerInfo {
        @NotBlank(message = "Customer mobile is required")
        @Pattern(regexp = "\\d{10}", message = "Mobile must be a 10-digit number")
        private String mobile;

        private String name;
        private String email;

        public CustomerInfo() {}
    }
}
