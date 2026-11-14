package com.bankingsystem.service;

import com.bankingsystem.dto.response.AdminDashboardStatsResponse;
import com.bankingsystem.entity.*;
import com.bankingsystem.entity.enums.ComplaintStatus;
import com.bankingsystem.entity.enums.RequestStatus;
import com.bankingsystem.entity.enums.TransactionStatus;
import com.bankingsystem.entity.enums.TransactionType;
import com.bankingsystem.exception.ResourceNotFoundException;
import com.bankingsystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private TransactionBlockRequestRepository blockRequestRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    public AdminDashboardStatsResponse getDashboardStats() {
        long totalCustomers = customerRepository.count();
        long activeCustomers = customerRepository.countByStatus(Customer.Status.ACTIVE);
        long blockedCustomers = customerRepository.countByStatus(Customer.Status.BLOCKED);
        long totalAccounts = accountRepository.count();
        long totalTransactions = transactionRepository.count();
        BigDecimal totalDeposits = transactionRepository.sumAllByType(TransactionType.DEPOSIT);
        BigDecimal totalWithdrawals = transactionRepository.sumAllByType(TransactionType.WITHDRAWAL);
        long pendingComplaints = complaintRepository.countByStatus(ComplaintStatus.OPEN);
        long pendingBlocks = blockRequestRepository.countByStatus(RequestStatus.PENDING);

        return new AdminDashboardStatsResponse(
                totalCustomers, activeCustomers, blockedCustomers, totalAccounts,
                totalTransactions, totalDeposits, totalWithdrawals,
                pendingComplaints, pendingBlocks
        );
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer toggleCustomerStatus(Long customerId, Customer.Status status) {
        Customer c = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        c.setStatus(status);
        return customerRepository.save(c);
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    @Transactional
    public Transaction setTransactionStatus(Long transactionId, TransactionStatus status) {
        Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        tx.setStatus(status);
        return transactionRepository.save(tx);
    }

    public List<TransactionBlockRequest> getAllBlockRequests() {
        return blockRequestRepository.findAll();
    }

    @Transactional
    public TransactionBlockRequest reviewBlockRequest(Long requestId, RequestStatus status, String remarks) {
        TransactionBlockRequest req = blockRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Block request not found"));

        req.setStatus(status);
        req.setAdminRemarks(remarks);
        req.setReviewedAt(LocalDateTime.now());

        if (status == RequestStatus.APPROVED) {
            req.getTransaction().setStatus(TransactionStatus.BLOCKED);
            transactionRepository.save(req.getTransaction());
        }

        return blockRequestRepository.save(req);
    }

    public List<AuditLog> getAuditLogs() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc();
    }
}
