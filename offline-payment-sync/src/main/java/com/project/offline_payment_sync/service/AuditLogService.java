

package com.project.offline_payment_sync.service;

import com.project.offline_payment_sync.entity.AuditLog;
import com.project.offline_payment_sync.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

   
    public void log(String action,
                    String status,
                    String fromUser,
                    String toUser,
                    Double amount,
                    String referenceId,
                    String message) {

        AuditLog log = new AuditLog(
                action,
                status,
                fromUser,
                toUser,
                amount,
                referenceId,
                message
        );

        auditLogRepository.save(log);
    }

    
    public void logSuccess(String action,
                           String fromUser,
                           String toUser,
                           Double amount,
                           String referenceId,
                           String message) {

        log(action, "SUCCESS", fromUser, toUser, amount, referenceId, message);
    }

   
    public void logFailure(String action,
                           String fromUser,
                           String toUser,
                           Double amount,
                           String referenceId,
                           String message) {

        log(action, "FAILED", fromUser, toUser, amount, referenceId, message);
    }
}