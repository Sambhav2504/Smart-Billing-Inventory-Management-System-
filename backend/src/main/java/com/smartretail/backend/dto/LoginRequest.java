package com.smartretail.backend.dto;

public class LoginRequest {
    private String email;
    private String password;

    // Default constructor (required by Spring)
    public LoginRequest() {
    }

    // Getters and Setters (required for Spring to read/write JSON)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}