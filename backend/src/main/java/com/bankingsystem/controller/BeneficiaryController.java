package com.bankingsystem.controller;

import com.bankingsystem.dto.request.BeneficiaryRequest;
import com.bankingsystem.dto.response.ApiResponse;
import com.bankingsystem.entity.Beneficiary;
import com.bankingsystem.entity.User;
import com.bankingsystem.repository.UserRepository;
import com.bankingsystem.service.BeneficiaryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beneficiaries")
public class BeneficiaryController {

    @Autowired
    private BeneficiaryService beneficiaryService;

    @Autowired
    private UserRepository userRepository;

    private Long getCustomerId(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        return user.getCustomer().getCustomerId();
    }

    @GetMapping
    public ResponseEntity<List<Beneficiary>> getBeneficiaries(Authentication auth) {
        return ResponseEntity.ok(beneficiaryService.getBeneficiaries(getCustomerId(auth)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> addBeneficiary(@Valid @RequestBody BeneficiaryRequest request, Authentication auth) {
        Beneficiary b = beneficiaryService.addBeneficiary(request, getCustomerId(auth));
        return ResponseEntity.ok(new ApiResponse(true, "Beneficiary added successfully", b));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateBeneficiary(@PathVariable Long id, @Valid @RequestBody BeneficiaryRequest request, Authentication auth) {
        Beneficiary b = beneficiaryService.updateBeneficiary(id, request, getCustomerId(auth));
        return ResponseEntity.ok(new ApiResponse(true, "Beneficiary updated successfully", b));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteBeneficiary(@PathVariable Long id, Authentication auth) {
        beneficiaryService.deleteBeneficiary(id, getCustomerId(auth));
        return ResponseEntity.ok(new ApiResponse(true, "Beneficiary deleted successfully", null));
    }
}
