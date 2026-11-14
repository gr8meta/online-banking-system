package com.bankingsystem.repository;

import com.bankingsystem.entity.Complaint;
import com.bankingsystem.entity.enums.ComplaintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByCustomerCustomerIdOrderByCreatedAtDesc(Long customerId);
    long countByStatus(ComplaintStatus status);
}
