package com.project.offline_payment_sync.repository;

import com.project.offline_payment_sync.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByReferenceId(String referenceId);

    List<AuditLog> findByStatus(String status);

    List<AuditLog> findByFromUser(String fromUser);
}