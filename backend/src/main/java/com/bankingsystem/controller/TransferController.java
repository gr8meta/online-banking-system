package com.bankingsystem.controller;

import com.bankingsystem.dto.request.TransferRequest;
import com.bankingsystem.dto.response.ApiResponse;
import com.bankingsystem.dto.response.TransactionResponse;
import com.bankingsystem.entity.User;
import com.bankingsystem.repository.UserRepository;
import com.bankingsystem.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    @Autowired
    private TransferService transferService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ApiResponse> transferFunds(@Valid @RequestBody TransferRequest request, Authentication auth) {
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        Long customerId = user.getCustomer().getCustomerId();

        TransactionResponse tx = transferService.executeTransfer(request, customerId);
        return ResponseEntity.ok(new ApiResponse(true, "Transfer completed successfully", tx));
    }
}
