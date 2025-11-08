package com.smartretail.backend.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Setter
@Getter
@Document(collection = "refresh_tokens")
public class RefreshToken {
    // Getters and Setters
    @Id
    private String id;
    private String userId;
    private String token;
    private Date expiryDate;

    // Constructors
    public RefreshToken() {}

    public RefreshToken(String userId, String token, Date expiryDate) {
        this.userId = userId;
        this.token = token;
        this.expiryDate = expiryDate;
    }

}