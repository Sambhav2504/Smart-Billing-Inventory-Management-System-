package com.smartretail.backend.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Setter
@Getter
@Document(collection = "push_subscriptions")
public class PushSubscription {
    // Getters and Setters
    @Id
    private String id;
    private String userId; // Linked to User
    private String endpoint;
    private String p256dh;
    private String auth;

    // Constructors
    public PushSubscription() {}

    public PushSubscription(String userId, String endpoint, String p256dh, String auth) {
        this.userId = userId;
        this.endpoint = endpoint;
        this.p256dh = p256dh;
        this.auth = auth;
    }

}