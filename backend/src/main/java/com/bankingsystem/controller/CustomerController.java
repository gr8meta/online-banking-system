package com.bankingsystem.controller;

import com.bankingsystem.dto.request.ChangePasswordRequest;
import com.bankingsystem.dto.request.ProfileUpdateRequest;
import com.bankingsystem.dto.response.ApiResponse;
import com.bankingsystem.dto.response.CustomerDashboardResponse;
import com.bankingsystem.dto.response.TransactionResponse;
import com.bankingsystem.entity.Account;
import com.bankingsystem.entity.Customer;
import com.bankingsystem.entity.User;
import com.bankingsystem.exception.BadRequestException;
import com.bankingsystem.exception.ResourceNotFoundException;
import com.bankingsystem.repository.AccountRepository;
import com.bankingsystem.repository.CustomerRepository;
import com.bankingsystem.repository.TransactionRepository;
import com.bankingsystem.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/profile")
    public ResponseEntity<Customer> getProfile(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(user.getCustomer());
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse> updateProfile(@Valid @RequestBody ProfileUpdateRequest request, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        Customer customer = user.getCustomer();

        customer.setFullName(request.fullName());
        customer.setAddress(request.address());
        customer.setCity(request.city());
        customer.setState(request.state());
        customer.setPinCode(request.pinCode());

        customerRepository.save(customer);
        return ResponseEntity.ok(new ApiResponse(true, "Profile updated successfully", customer));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BadRequestException("Current password does not match");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(new ApiResponse(true, "Password changed successfully", null));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<CustomerDashboardResponse> getDashboard(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Customer customer = user.getCustomer();

        List<Account> accounts = accountRepository.findByCustomerCustomerId(customer.getCustomerId());
        Account primaryAccount = accounts.isEmpty() ? null : accounts.get(0);

        Long accId = primaryAccount != null ? primaryAccount.getAccountId() : null;
        String accNo = primaryAccount != null ? primaryAccount.getAccountNumber() : "N/A";
        BigDecimal balance = primaryAccount != null ? primaryAccount.getBalance() : BigDecimal.ZERO;
        String accType = primaryAccount != null ? primaryAccount.getAccountType().name() : "N/A";

        BigDecimal deposits = primaryAccount != null ? transactionRepository.sumDepositsByAccount(primaryAccount.getAccountId()) : BigDecimal.ZERO;
        BigDecimal withdrawals = primaryAccount != null ? transactionRepository.sumWithdrawalsByAccount(primaryAccount.getAccountId()) : BigDecimal.ZERO;

        List<TransactionResponse> recentTx = primaryAccount != null ?
                transactionRepository.findByAccountId(primaryAccount.getAccountId()).stream().limit(5)
                        .map(tx -> new TransactionResponse(
                                tx.getTransactionId(),
                                tx.getReferenceId(),
                                tx.getTransactionType().name(),
                                tx.getAmount(),
                                tx.getDescription(),
                                tx.getStatus().name(),
                                tx.getTransactionDate(),
                                tx.getSenderAccount() != null ? tx.getSenderAccount().getAccountNumber() : "N/A",
                                tx.getReceiverAccount() != null ? tx.getReceiverAccount().getAccountNumber() : "N/A"
                        )).toList() : List.of();

        return ResponseEntity.ok(new CustomerDashboardResponse(
                customer.getFullName(), accId, accNo, balance, accType, deposits, withdrawals, recentTx
        ));
    }
}
