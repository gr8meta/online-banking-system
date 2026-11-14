package com.bankingsystem.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record TransferRequest(
    @NotNull(message = "Sender Account ID is required") Long senderAccountId,
    @NotBlank(message = "Beneficiary Account Number is required") String beneficiaryAccountNumber,
    @NotNull @DecimalMin(value = "1.00", message = "Minimum transfer is ₹1.00") BigDecimal amount,
    String description,
    @NotBlank(message = "Transaction PIN or Verification Code is required") String transactionPin
) {}
