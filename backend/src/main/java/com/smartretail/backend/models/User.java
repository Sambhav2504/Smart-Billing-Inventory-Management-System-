package com.smartretail.backend.models;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Data
@Document(collection = "users")
public class User {
    private String userId;      // Custom unique ID (e.g., "u101")
    private String name;
    private String email;
    private String password;    // This field will store the hashed password
    private String role;        // "OWNER", "MANAGER", "CASHIER"
    private Date createdAt;
    private Date lastLogin;

    // Custom 5-parameter constructor for AuthService
    public User(String userId, String email, String password, String name, String role) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
        this.createdAt = new Date(); // Set createdAt to current date
        this.lastLogin = null;      // Set lastLogin to null initially
    }
}