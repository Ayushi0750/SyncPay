
package com.project.offline_payment_sync.repository;

import com.project.offline_payment_sync.entity.Transaction;
import com.project.offline_payment_sync.entity.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    boolean existsByTransactionId(String transactionId);

    
    Optional<Transaction> findByTransactionId(String transactionId);

    List<Transaction> findByStatus(TransactionStatus status);

    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Transaction> findTop10ByStatus(TransactionStatus status);

    
    @Query("SELECT t FROM Transaction t WHERE t.status = com.project.offline_payment_sync.entity.enums.TransactionStatus.PROCESSING AND t.lastUpdated < :cutoff")
    List<Transaction> findStuckProcessing(@Param("cutoff") LocalDateTime cutoff);

    
    
    
    List<Transaction> findByFromEmailOrToEmailOrderByTimestampDesc(String fromEmail, String toEmail);
    
    
    List<Transaction> findByFromEmailAndStatus(String fromEmail, TransactionStatus status);
}