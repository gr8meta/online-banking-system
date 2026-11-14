package com.bankingsystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TransactionBlockRequestDto(
    @NotNull(message = "Transaction ID is required") Long transactionId,
    @NotBlank(message = "Reason is required") String reason
) {}
