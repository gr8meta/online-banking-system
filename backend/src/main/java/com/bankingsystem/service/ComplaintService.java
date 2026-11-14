package com.bankingsystem.service;

import com.bankingsystem.dto.request.ComplaintRequest;
import com.bankingsystem.dto.request.ComplaintResponseRequest;
import com.bankingsystem.entity.Complaint;
import com.bankingsystem.entity.Customer;
import com.bankingsystem.entity.Transaction;
import com.bankingsystem.entity.enums.ComplaintStatus;
import com.bankingsystem.exception.ResourceNotFoundException;
import com.bankingsystem.repository.ComplaintRepository;
import com.bankingsystem.repository.CustomerRepository;
import com.bankingsystem.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ComplaintService {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public List<Complaint> getCustomerComplaints(Long customerId) {
        return complaintRepository.findByCustomerCustomerIdOrderByCreatedAtDesc(customerId);
    }

    public Complaint submitComplaint(ComplaintRequest request, Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Transaction transaction = null;
        if (request.transactionId() != null) {
            transaction = transactionRepository.findById(request.transactionId()).orElse(null);
        }

        Complaint complaint = Complaint.builder()
                .customer(customer)
                .transaction(transaction)
                .subject(request.subject())
                .description(request.description())
                .status(ComplaintStatus.OPEN)
                .build();

        return complaintRepository.save(complaint);
    }

    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAll();
    }

    public Complaint respondToComplaint(Long complaintId, ComplaintResponseRequest request) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));

        complaint.setAdminResponse(request.adminResponse());
        complaint.setStatus(ComplaintStatus.valueOf(request.status()));
        return complaintRepository.save(complaint);
    }
}
