package com.bankingsystem.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record WithdrawRequest(
    @NotNull(message = "Account ID is required") Long accountId,
    @NotNull @DecimalMin(value = "1.00", message = "Minimum withdrawal is ₹1.00") BigDecimal amount,
    String description
) {}
