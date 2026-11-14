package com.bankingsystem.dto.response;

import java.math.BigDecimal;

public record AccountSummaryResponse(
    Long accountId,
    String accountNumber,
    String accountType,
    BigDecimal balance,
    String status
) {}
