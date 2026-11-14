package com.bankingsystem.dto.response;

public record AuthResponse(
    String token,
    String username,
    String role,
    Long customerId,
    String message
) {}
