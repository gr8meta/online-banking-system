package com.bankingsystem.controller;

import com.bankingsystem.dto.request.ComplaintRequest;
import com.bankingsystem.dto.response.ApiResponse;
import com.bankingsystem.entity.Complaint;
import com.bankingsystem.entity.User;
import com.bankingsystem.repository.UserRepository;
import com.bankingsystem.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Complaint>> getCustomerComplaints(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        return ResponseEntity.ok(complaintService.getCustomerComplaints(user.getCustomer().getCustomerId()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> submitComplaint(@Valid @RequestBody ComplaintRequest request, Authentication auth) {
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        Complaint c = complaintService.submitComplaint(request, user.getCustomer().getCustomerId());
        return ResponseEntity.ok(new ApiResponse(true, "Complaint registered successfully", c));
    }
}
