package com.bankingsystem.service;

import com.bankingsystem.dto.request.TransferRequest;
import com.bankingsystem.dto.response.TransactionResponse;
import com.bankingsystem.entity.Account;
import com.bankingsystem.entity.Transaction;
import com.bankingsystem.entity.enums.AccountStatus;
import com.bankingsystem.entity.enums.TransactionStatus;
import com.bankingsystem.entity.enums.TransactionType;
import com.bankingsystem.exception.BadRequestException;
import com.bankingsystem.exception.InsufficientBalanceException;
import com.bankingsystem.exception.ResourceNotFoundException;
import com.bankingsystem.repository.AccountRepository;
import com.bankingsystem.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class TransferService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AuditService auditService;

    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    public TransactionResponse executeTransfer(TransferRequest request, Long authenticatedCustomerId) {
        Account sender = accountRepository.findById(request.senderAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Sender account not found"));

        if (!sender.getCustomer().getCustomerId().equals(authenticatedCustomerId)) {
            throw new BadRequestException("Unauthorized access to sender account");
        }

        if (sender.getStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("Sender account is not active");
        }

        Account receiver = accountRepository.findByAccountNumber(request.beneficiaryAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary account not found"));

        if (receiver.getStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("Beneficiary account is currently inactive");
        }

        if (sender.getAccountNumber().equals(receiver.getAccountNumber())) {
            throw new BadRequestException("Cannot transfer money to the same account");
        }

        BigDecimal transferAmount = request.amount();
        if (sender.getBalance().compareTo(transferAmount) < 0) {
            throw new InsufficientBalanceException("Insufficient account balance for this transfer");
        }

        // Deduct from sender and credit to receiver
        sender.setBalance(sender.getBalance().subtract(transferAmount));
        receiver.setBalance(receiver.getBalance().add(transferAmount));

        accountRepository.save(sender);
        accountRepository.save(receiver);

        Transaction transaction = Transaction.builder()
                .referenceId(UUID.randomUUID().toString())
                .senderAccount(sender)
                .receiverAccount(receiver)
                .transactionType(TransactionType.TRANSFER)
                .amount(transferAmount)
                .description(request.description() != null && !request.description().isBlank() 
                        ? request.description() : "Fund Transfer to A/C " + receiver.getAccountNumber())
                .status(TransactionStatus.SUCCESS)
                .build();

        Transaction savedTx = transactionRepository.save(transaction);
        auditService.logAction(authenticatedCustomerId, "TRANSFER",
                "Transferred ₹" + transferAmount + " to Account " + receiver.getAccountNumber());

        return new TransactionResponse(
                savedTx.getTransactionId(),
                savedTx.getReferenceId(),
                savedTx.getTransactionType().name(),
                savedTx.getAmount(),
                savedTx.getDescription(),
                savedTx.getStatus().name(),
                savedTx.getTransactionDate(),
                sender.getAccountNumber(),
                receiver.getAccountNumber()
        );
    }
}
