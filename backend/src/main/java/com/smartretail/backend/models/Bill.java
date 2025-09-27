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
    private String addedBy; // Make sure this field exists

    @Setter
    @Getter
    public static class BillItem {
        private String productId;
        private int qty;
        private double price;
    }

    @Setter
    @Getter
    public static class CustomerInfo {
        private String name;
        private String email;
        private String mobile;
    }
}