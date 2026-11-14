package com.bankingsystem.dto.response;

import java.math.BigDecimal;

public record AdminDashboardStatsResponse(
    long totalCustomers,
    long activeCustomers,
    long blockedCustomers,
    long totalAccounts,
    long totalTransactions,
    BigDecimal totalDeposits,
    BigDecimal totalWithdrawals,
    long pendingComplaints,
    long pendingBlockRequests
) {}
