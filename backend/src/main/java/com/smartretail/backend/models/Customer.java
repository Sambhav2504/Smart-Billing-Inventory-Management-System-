package com.smartretail.backend.models;

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
    private String name;
    private String email;
    private String mobile;
    private Date createdAt;
    private List<String> purchaseHistory;
    private int totalPurchaseCount;
    private Date lastPurchaseDate;

    public Customer() {
        this.purchaseHistory = new ArrayList<>();
        this.createdAt = new Date();
        this.totalPurchaseCount = 0;
        this.lastPurchaseDate = null;
    }

    public Customer(String name, String email, String mobile) {
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.createdAt = new Date();
        this.purchaseHistory = new ArrayList<>();
        this.totalPurchaseCount = 0;
        this.lastPurchaseDate = null;
    }

    public void addBillId(String billId) {
        if (this.purchaseHistory == null) {
            this.purchaseHistory = new ArrayList<>();
        }
        this.purchaseHistory.add(billId);
    }
}