package com.bankingsystem.dto.response;

public record ApiResponse(
    boolean success,
    String message,
    Object data
) {}
