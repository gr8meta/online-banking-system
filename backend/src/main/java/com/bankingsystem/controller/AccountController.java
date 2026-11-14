package com.bankingsystem.controller;

import com.bankingsystem.dto.request.DepositRequest;
import com.bankingsystem.dto.request.WithdrawRequest;
import com.bankingsystem.dto.response.AccountSummaryResponse;
import com.bankingsystem.dto.response.ApiResponse;
import com.bankingsystem.dto.response.TransactionResponse;
import com.bankingsystem.entity.User;
import com.bankingsystem.repository.UserRepository;
import com.bankingsystem.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserRepository userRepository;

    private Long getCustomerId(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        return user.getCustomer() != null ? user.getCustomer().getCustomerId() : null;
    }

    @GetMapping
    public ResponseEntity<List<AccountSummaryResponse>> getAccounts(Authentication auth) {
        return ResponseEntity.ok(accountService.getCustomerAccounts(getCustomerId(auth)));
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<ApiResponse> getBalance(@PathVariable Long id, Authentication auth) {
        BigDecimal balance = accountService.getBalance(id, getCustomerId(auth));
        return ResponseEntity.ok(new ApiResponse(true, "Balance retrieved", balance));
    }

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse> deposit(@Valid @RequestBody DepositRequest request, Authentication auth) {
        TransactionResponse tx = accountService.deposit(request, getCustomerId(auth));
        return ResponseEntity.ok(new ApiResponse(true, "Deposit successful", tx));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse> withdraw(@Valid @RequestBody WithdrawRequest request, Authentication auth) {
        TransactionResponse tx = accountService.withdraw(request, getCustomerId(auth));
        return ResponseEntity.ok(new ApiResponse(true, "Withdrawal successful", tx));
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactions(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(accountService.getAccountTransactions(id, getCustomerId(auth)));
    }
}
