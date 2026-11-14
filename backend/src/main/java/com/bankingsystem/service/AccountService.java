package com.bankingsystem.service;

import com.bankingsystem.dto.request.DepositRequest;
import com.bankingsystem.dto.request.WithdrawRequest;
import com.bankingsystem.dto.response.AccountSummaryResponse;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AuditService auditService;

    public List<AccountSummaryResponse> getCustomerAccounts(Long customerId) {
        return accountRepository.findByCustomerCustomerId(customerId).stream()
                .map(acc -> new AccountSummaryResponse(
                        acc.getAccountId(),
                        acc.getAccountNumber(),
                        acc.getAccountType().name(),
                        acc.getBalance(),
                        acc.getStatus().name()
                )).toList();
    }

    public BigDecimal getBalance(Long accountId, Long customerId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        if (!account.getCustomer().getCustomerId().equals(customerId)) {
            throw new BadRequestException("Unauthorized access to account");
        }
        return account.getBalance();
    }

    @Transactional
    public TransactionResponse deposit(DepositRequest request, Long customerId) {
        Account account = accountRepository.findById(request.accountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (!account.getCustomer().getCustomerId().equals(customerId)) {
            throw new BadRequestException("Unauthorized account access");
        }
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("Account is not active");
        }

        account.setBalance(account.getBalance().add(request.amount()));
        accountRepository.save(account);

        Transaction tx = Transaction.builder()
                .referenceId(UUID.randomUUID().toString())
                .receiverAccount(account)
                .transactionType(TransactionType.DEPOSIT)
                .amount(request.amount())
                .description(request.description() != null ? request.description() : "Cash Deposit")
                .status(TransactionStatus.SUCCESS)
                .build();

        Transaction savedTx = transactionRepository.save(tx);
        auditService.logAction(customerId, "DEPOSIT", "Deposited ₹" + request.amount() + " to account " + account.getAccountNumber());

        return mapToTxResponse(savedTx);
    }

    @Transactional
    public TransactionResponse withdraw(WithdrawRequest request, Long customerId) {
        Account account = accountRepository.findById(request.accountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (!account.getCustomer().getCustomerId().equals(customerId)) {
            throw new BadRequestException("Unauthorized account access");
        }
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("Account is not active");
        }
        if (account.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException("Insufficient account balance");
        }

        account.setBalance(account.getBalance().subtract(request.amount()));
        accountRepository.save(account);

        Transaction tx = Transaction.builder()
                .referenceId(UUID.randomUUID().toString())
                .senderAccount(account)
                .transactionType(TransactionType.WITHDRAWAL)
                .amount(request.amount())
                .description(request.description() != null ? request.description() : "ATM/Cash Withdrawal")
                .status(TransactionStatus.SUCCESS)
                .build();

        Transaction savedTx = transactionRepository.save(tx);
        auditService.logAction(customerId, "WITHDRAWAL", "Withdrew ₹" + request.amount() + " from account " + account.getAccountNumber());

        return mapToTxResponse(savedTx);
    }

    public List<TransactionResponse> getAccountTransactions(Long accountId, Long customerId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        if (!account.getCustomer().getCustomerId().equals(customerId)) {
            throw new BadRequestException("Unauthorized account access");
        }

        return transactionRepository.findByAccountId(accountId).stream()
                .map(this::mapToTxResponse)
                .toList();
    }

    private TransactionResponse mapToTxResponse(Transaction tx) {
        String senderAcc = tx.getSenderAccount() != null ? tx.getSenderAccount().getAccountNumber() : "N/A";
        String receiverAcc = tx.getReceiverAccount() != null ? tx.getReceiverAccount().getAccountNumber() : "N/A";
        return new TransactionResponse(
                tx.getTransactionId(),
                tx.getReferenceId(),
                tx.getTransactionType().name(),
                tx.getAmount(),
                tx.getDescription(),
                tx.getStatus().name(),
                tx.getTransactionDate(),
                senderAcc,
                receiverAcc
        );
    }
}
