package com.bankingsystem.controller;

import com.bankingsystem.dto.request.TransactionBlockRequestDto;
import com.bankingsystem.dto.response.ApiResponse;
import com.bankingsystem.entity.*;
import com.bankingsystem.exception.ResourceNotFoundException;
import com.bankingsystem.repository.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transaction-block-requests")
public class TransactionBlockController {

    @Autowired
    private TransactionBlockRequestRepository blockRequestRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<TransactionBlockRequest>> getMyRequests(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        return ResponseEntity.ok(blockRequestRepository.findByCustomerCustomerIdOrderByCreatedAtDesc(user.getCustomer().getCustomerId()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> requestBlock(@Valid @RequestBody TransactionBlockRequestDto dto, Authentication auth) {
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        Transaction tx = transactionRepository.findById(dto.transactionId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        TransactionBlockRequest request = TransactionBlockRequest.builder()
                .customer(user.getCustomer())
                .transaction(tx)
                .reason(dto.reason())
                .build();

        TransactionBlockRequest saved = blockRequestRepository.save(request);
        return ResponseEntity.ok(new ApiResponse(true, "Block request submitted for admin review", saved));
    }
}
