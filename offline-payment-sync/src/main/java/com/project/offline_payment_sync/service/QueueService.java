

package com.project.offline_payment_sync.service;

import com.project.offline_payment_sync.dto.QueueStoreRequest;
import com.project.offline_payment_sync.entity.PendingTransaction;
import com.project.offline_payment_sync.repository.PendingTransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueueService {

    private final PendingTransactionRepository pendingTransactionRepository;

    public QueueService(PendingTransactionRepository pendingTransactionRepository) {
        this.pendingTransactionRepository = pendingTransactionRepository;
    }

    public String storePendingTransaction(
            String senderEmail,
            QueueStoreRequest request) {

        if (pendingTransactionRepository.existsByTransactionId(
                request.getTransactionId())) {

            return "Duplicate transaction ignored";
        }

        PendingTransaction tx = new PendingTransaction();

        tx.setTransactionId(request.getTransactionId());
        tx.setFromEmail(senderEmail);
        tx.setToEmail(request.getToEmail());
        tx.setAmount(request.getAmount());
        tx.setStatus("PENDING");

        pendingTransactionRepository.save(tx);

        return "Transaction added to queue";
    }

    public List<PendingTransaction> getPendingTransactions(
            String senderEmail) {

        return pendingTransactionRepository
                .findByFromEmailAndStatus(
                        senderEmail,
                        "PENDING"
                );
    }
}