package com.bankingsystem.service;

import com.bankingsystem.entity.AuditLog;
import com.bankingsystem.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void logAction(Long userId, String action, String description) {
        try {
            AuditLog log = AuditLog.builder()
                    .userId(userId)
                    .action(action)
                    .description(description)
                    .ipAddress("127.0.0.1")
                    .build();
            auditLogRepository.save(log);
        } catch (Exception ignored) {}
    }
}
