

package com.project.offline_payment_sync.repository;

import com.project.offline_payment_sync.entity.TransactionAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionAuditRepository extends JpaRepository<TransactionAudit, Long> {

    List<TransactionAudit> findByTransactionId(String transactionId);
}