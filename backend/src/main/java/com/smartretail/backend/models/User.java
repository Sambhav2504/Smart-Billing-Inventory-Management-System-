package com.smartretail.backend.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "users")
public class User {
    @Id
    private String id; // Maps to _id (MongoDB-generated ObjectId stored as String)
    private String userId; // Custom unique ID (e.g., "u101")
    @Indexed(unique = true)
    private String email; // Unique email
    private String name;
    private String password; // Hashed password
    private String role; // "OWNER", "MANAGER", "CASHIER"
    private Date createdAt;
    private Date lastLogin;

    public User(String userId, String email, String password, String name, String role) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
        this.createdAt = new Date();
        this.lastLogin = null;
    }
}