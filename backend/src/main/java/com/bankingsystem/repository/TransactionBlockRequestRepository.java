package com.bankingsystem.repository;

import com.bankingsystem.entity.TransactionBlockRequest;
import com.bankingsystem.entity.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionBlockRequestRepository extends JpaRepository<TransactionBlockRequest, Long> {
    List<TransactionBlockRequest> findByCustomerCustomerIdOrderByCreatedAtDesc(Long customerId);
    long countByStatus(RequestStatus status);
}
