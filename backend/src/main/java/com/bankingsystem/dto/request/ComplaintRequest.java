package com.bankingsystem.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ComplaintRequest(
    Long transactionId,
    @NotBlank(message = "Subject is required") String subject,
    @NotBlank(message = "Description is required") String description
) {}
