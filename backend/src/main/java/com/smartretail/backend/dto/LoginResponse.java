package com.smartretail.backend.dto;

public class LoginResponse {
    private String token;
    private String userId;
    private String name;
    private String role;

    // Constructor to easily create a response
    public LoginResponse(String token, String userId, String name, String role) {
        this.token = token;
        this.userId = userId;
        this.name = name;
        this.role = role;
    }

    // Getters (Spring uses these to convert the object back to JSON)
    public String getToken() {
        return token;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }
}