
package com.project.offline_payment_sync.service;

import com.project.offline_payment_sync.entity.*;
import com.project.offline_payment_sync.entity.enums.TransactionStatus;
import com.project.offline_payment_sync.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminService {

    private final TransactionRepository transactionRepository;
    private final DeadLetterTransactionRepository dlqRepository;

    public AdminService(TransactionRepository transactionRepository,
                        DeadLetterTransactionRepository dlqRepository) {
        this.transactionRepository = transactionRepository;
        this.dlqRepository = dlqRepository;
    }

   
    public long getPendingCount() {
        return transactionRepository.findByStatus(TransactionStatus.PENDING).size();
    }

    public long getSuccessCount() {
        return transactionRepository.findByStatus(TransactionStatus.SUCCESS).size();
    }

    public long getFailedCount() {
        return transactionRepository.findByStatus(TransactionStatus.FINAL_FAILED).size();
    }

    
    public List<Transaction> getFailedTransactions() {
        return transactionRepository.findByStatus(TransactionStatus.FINAL_FAILED);
    }

    public List<DeadLetterTransaction> getDeadLetterTransactions() {
        return dlqRepository.findAll();
    }

   

    @Transactional
    public String retryFailedTransaction(String transactionId) {

        Transaction tx = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (tx.getStatus() != TransactionStatus.FINAL_FAILED) {
            return "Only FINAL_FAILED transactions can be retried";
        }

        tx.setStatus(TransactionStatus.PENDING);
        tx.setRetryCount(0);
        tx.setLastUpdated(LocalDateTime.now());

        transactionRepository.save(tx);

        return "Transaction reset for retry";
    }

    @Transactional
    public String reprocessDlq(String transactionId) {

        DeadLetterTransaction dlq = dlqRepository.findAll().stream()
                .filter(t -> t.getTransactionId().equals(transactionId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("DLQ entry not found"));

        Transaction tx = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        tx.setStatus(TransactionStatus.PENDING);
        tx.setRetryCount(0);
        tx.setLastUpdated(LocalDateTime.now());

        transactionRepository.save(tx);

        dlqRepository.delete(dlq);

        return "DLQ transaction requeued successfully";
    }

    @Transactional
    public String forceResetTransaction(String transactionId) {

        Transaction tx = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        tx.setStatus(TransactionStatus.PENDING);
        tx.setRetryCount(0);
        tx.setLastUpdated(LocalDateTime.now());

        transactionRepository.save(tx);

        return "Transaction force reset to PENDING";
    }
}