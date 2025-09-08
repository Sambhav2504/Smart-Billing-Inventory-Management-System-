package com.smartretail.backend.models;

import lombok.Data;

import java.util.Date;

@Data
public class User {
    private String userId;
    private String name;
    private String email;
    private String password; // Stores hashed value
    private String role;
    private Date createdAt;
    private Date lastLogin;
}