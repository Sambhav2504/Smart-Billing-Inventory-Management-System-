package com.smartretail.backend.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "customers")
public class Customer {
    @Id
    private String id; // Maps to _id
    private String name;
    private String email; // Nullable
    @Indexed(unique = true, sparse = true)
    private String mobile; // Unique, nullable
    private Date createdAt;
}