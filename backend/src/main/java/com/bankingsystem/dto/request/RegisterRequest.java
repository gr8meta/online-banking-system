package com.bankingsystem.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record RegisterRequest(
    @NotBlank(message = "Full name is required") String fullName,
    @NotNull(message = "Date of Birth is required") LocalDate dateOfBirth,
    @NotBlank(message = "Gender is required") String gender,
    @Email(message = "Invalid email format") @NotBlank String email,
    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile must be 10 digits") String mobile,
    @NotBlank String address,
    @NotBlank String city,
    @NotBlank String state,
    @Pattern(regexp = "^[0-9]{6}$", message = "PIN Code must be 6 digits") String pinCode,
    @Size(min = 4, max = 20, message = "Username must be 4-20 chars") String username,
    @Size(min = 6, message = "Password must be at least 6 chars") String password
) {}
