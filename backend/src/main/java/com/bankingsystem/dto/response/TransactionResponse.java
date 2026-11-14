package com.bankingsystem.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
    Long transactionId,
    String referenceId,
    String type,
    BigDecimal amount,
    String description,
    String status,
    LocalDateTime date,
    String senderAccountNumber,
    String receiverAccountNumber
) {}
