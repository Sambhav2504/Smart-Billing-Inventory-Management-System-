package com.smartretail.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginResponse {
    // Getters and Setters
    private String accessToken;
    private String refreshToken;

    public LoginResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

}