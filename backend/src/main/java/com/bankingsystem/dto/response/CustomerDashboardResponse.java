package com.bankingsystem.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CustomerDashboardResponse(
    String customerName,
    Long accountId,
    String accountNumber,
    BigDecimal availableBalance,
    String accountType,
    BigDecimal totalDeposits,
    BigDecimal totalWithdrawals,
    List<TransactionResponse> recentTransactions
) {}
