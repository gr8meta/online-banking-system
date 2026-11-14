package com.bankingsystem.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ComplaintResponseRequest(
    @NotBlank(message = "Admin response is required") String adminResponse,
    @NotBlank(message = "Status is required") String status
) {}
