package com.bankingsystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ProfileUpdateRequest(
    @NotBlank String fullName,
    @NotBlank String address,
    @NotBlank String city,
    @NotBlank String state,
    @Pattern(regexp = "^[0-9]{6}$", message = "PIN Code must be 6 digits") String pinCode
) {}
