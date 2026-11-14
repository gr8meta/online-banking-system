package com.bankingsystem.controller;

import com.bankingsystem.dto.request.ComplaintResponseRequest;
import com.bankingsystem.dto.response.AdminDashboardStatsResponse;
import com.bankingsystem.dto.response.ApiResponse;
import com.bankingsystem.entity.*;
import com.bankingsystem.entity.enums.RequestStatus;
import com.bankingsystem.entity.enums.TransactionStatus;
import com.bankingsystem.service.AdminService;
import com.bankingsystem.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private ComplaintService complaintService;

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardStatsResponse> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/customers")
    public ResponseEntity<List<Customer>> getAllCustomers() {
        return ResponseEntity.ok(adminService.getAllCustomers());
    }

    @PutMapping("/customers/{id}/status")
    public ResponseEntity<ApiResponse> updateCustomerStatus(@PathVariable Long id, @RequestParam Customer.Status status) {
        Customer c = adminService.toggleCustomerStatus(id, status);
        return ResponseEntity.ok(new ApiResponse(true, "Customer status updated to " + status, c));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(adminService.getAllTransactions());
    }

    @PutMapping("/transactions/{id}/status")
    public ResponseEntity<ApiResponse> setTransactionStatus(@PathVariable Long id, @RequestParam TransactionStatus status) {
        Transaction tx = adminService.setTransactionStatus(id, status);
        return ResponseEntity.ok(new ApiResponse(true, "Transaction status updated", tx));
    }

    @GetMapping("/complaints")
    public ResponseEntity<List<Complaint>> getAllComplaints() {
        return ResponseEntity.ok(complaintService.getAllComplaints());
    }

    @PutMapping("/complaints/{id}/respond")
    public ResponseEntity<ApiResponse> respondToComplaint(@PathVariable Long id, @Valid @RequestBody ComplaintResponseRequest request) {
        Complaint c = complaintService.respondToComplaint(id, request);
        return ResponseEntity.ok(new ApiResponse(true, "Complaint response submitted", c));
    }

    @GetMapping("/block-requests")
    public ResponseEntity<List<TransactionBlockRequest>> getAllBlockRequests() {
        return ResponseEntity.ok(adminService.getAllBlockRequests());
    }

    @PutMapping("/block-requests/{id}/review")
    public ResponseEntity<ApiResponse> reviewBlockRequest(@PathVariable Long id, @RequestParam RequestStatus status, @RequestParam(required = false) String remarks) {
        TransactionBlockRequest req = adminService.reviewBlockRequest(id, status, remarks);
        return ResponseEntity.ok(new ApiResponse(true, "Block request review finalized", req));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        return ResponseEntity.ok(adminService.getAuditLogs());
    }
}
