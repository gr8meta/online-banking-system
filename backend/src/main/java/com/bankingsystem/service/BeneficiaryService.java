package com.bankingsystem.service;

import com.bankingsystem.dto.request.BeneficiaryRequest;
import com.bankingsystem.entity.Beneficiary;
import com.bankingsystem.entity.Customer;
import com.bankingsystem.exception.BadRequestException;
import com.bankingsystem.exception.ResourceNotFoundException;
import com.bankingsystem.repository.BeneficiaryRepository;
import com.bankingsystem.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BeneficiaryService {

    @Autowired
    private BeneficiaryRepository beneficiaryRepository;

    @Autowired
    private CustomerRepository customerRepository;

    public List<Beneficiary> getBeneficiaries(Long customerId) {
        return beneficiaryRepository.findByCustomerCustomerId(customerId);
    }

    public Beneficiary addBeneficiary(BeneficiaryRequest request, Long customerId) {
        if (beneficiaryRepository.existsByCustomerCustomerIdAndAccountNumber(customerId, request.accountNumber())) {
            throw new BadRequestException("Beneficiary with this account number already exists");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Beneficiary beneficiary = Beneficiary.builder()
                .customer(customer)
                .beneficiaryName(request.beneficiaryName())
                .accountNumber(request.accountNumber())
                .ifscCode(request.ifscCode())
                .bankName(request.bankName())
                .nickname(request.nickname())
                .status(Beneficiary.Status.ACTIVE)
                .build();

        return beneficiaryRepository.save(beneficiary);
    }

    public Beneficiary updateBeneficiary(Long id, BeneficiaryRequest request, Long customerId) {
        Beneficiary b = beneficiaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary not found"));
        if (!b.getCustomer().getCustomerId().equals(customerId)) {
            throw new BadRequestException("Unauthorized access to beneficiary");
        }

        b.setBeneficiaryName(request.beneficiaryName());
        b.setIfscCode(request.ifscCode());
        b.setBankName(request.bankName());
        b.setNickname(request.nickname());

        return beneficiaryRepository.save(b);
    }

    public void deleteBeneficiary(Long id, Long customerId) {
        Beneficiary b = beneficiaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary not found"));
        if (!b.getCustomer().getCustomerId().equals(customerId)) {
            throw new BadRequestException("Unauthorized access to beneficiary");
        }
        beneficiaryRepository.delete(b);
    }
}
