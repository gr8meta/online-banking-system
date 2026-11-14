package com.bankingsystem.dto.request;

import jakarta.validation.constraints.NotBlank;

public record BeneficiaryRequest(
    @NotBlank(message = "Beneficiary name is required") String beneficiaryName,
    @NotBlank(message = "Account number is required") String accountNumber,
    @NotBlank(message = "IFSC code is required") String ifscCode,
    @NotBlank(message = "Bank name is required") String bankName,
    String nickname
) {}
