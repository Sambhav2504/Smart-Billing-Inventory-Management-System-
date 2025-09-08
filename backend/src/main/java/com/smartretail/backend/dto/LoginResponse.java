package com.smartretail.backend.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String userId;
    private String name;
    private String role;

    public LoginResponse(String token, String userId, String name, String role) {
        this.token = token;
        this.userId = userId;
        this.name = name;
        this.role = role;
    }
}