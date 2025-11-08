package com.smartretail.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CustomEmailRequest {

    @NotEmpty(message = "Customer ID list cannot be empty")
    private List<String> customerIds;

    @NotBlank(message = "Subject cannot be empty")
    private String subject;

    @NotBlank(message = "Body cannot be empty")
    private String body;
}