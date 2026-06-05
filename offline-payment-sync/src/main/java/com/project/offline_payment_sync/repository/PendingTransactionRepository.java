

package com.project.offline_payment_sync.repository;

import com.project.offline_payment_sync.entity.PendingTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PendingTransactionRepository
        extends JpaRepository<PendingTransaction, Long> {

    boolean existsByTransactionId(String transactionId);

    List<PendingTransaction> findByStatus(String status);

    List<PendingTransaction> findByFromEmailAndStatus(
            String fromEmail,
            String status
    );
}