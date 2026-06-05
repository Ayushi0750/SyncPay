package com.project.offline_payment_sync.service;

import com.project.offline_payment_sync.entity.*;
import com.project.offline_payment_sync.entity.enums.TransactionStatus;
import com.project.offline_payment_sync.repository.*;
import com.project.offline_payment_sync.security.CryptoService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class QueueSyncService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final WalletService walletService;
    private final IdempotencyRepository idempotencyRepository;
    private final TransactionAuditRepository auditRepository;
    private final CryptoService cryptoService;

    private final ReentrantLock lock = new ReentrantLock();

    public QueueSyncService(TransactionRepository transactionRepository,
                            UserRepository userRepository,
                            WalletService walletService,
                            IdempotencyRepository idempotencyRepository,
                            TransactionAuditRepository auditRepository,
                            CryptoService cryptoService) {

        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.walletService = walletService;
        this.idempotencyRepository = idempotencyRepository;
        this.auditRepository = auditRepository;
        this.cryptoService = cryptoService;
    }

    
    @Scheduled(fixedDelay = 5000)
    @Transactional 
    public void autoSyncProcessor() {

        if (!lock.tryLock()) return;

        try {
            processPendingTransactions();
        } finally {
            lock.unlock();
        }
    }

    
    @Scheduled(fixedDelay = 10000)
    @Transactional  
    public void recoverStuckTransactions() {

        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(2);

        List<Transaction> stuckList =
                transactionRepository.findStuckProcessing(cutoff);

        for (Transaction tx : stuckList) {

            tx.setStatus(TransactionStatus.PENDING);
            tx.setLastUpdated(LocalDateTime.now());
            transactionRepository.save(tx);

            auditRepository.save(new TransactionAudit(
                    tx.getTransactionId(),
                    TransactionStatus.PENDING,
                    "Recovered from stuck PROCESSING state"
            ));
        }
    }

    
    @Transactional
    public void processPendingTransactions() {

        List<Transaction> pendingList =
                transactionRepository.findTop10ByStatus(TransactionStatus.PENDING);

        for (Transaction tx : pendingList) {

            if (tx.getStatus() == TransactionStatus.SUCCESS ||
                tx.getStatus() == TransactionStatus.FINAL_FAILED) {
                continue;
            }

            
            LocalDateTime now = LocalDateTime.now();

            if (tx.getTimestamp() != null &&
                tx.getTimestamp().isBefore(now.minusMinutes(10))) {

                tx.setStatus(TransactionStatus.FINAL_FAILED);
                tx.setFailureReason("REPLAY BLOCKED - stale transaction");

                transactionRepository.save(tx);

                auditRepository.save(new TransactionAudit(
                        tx.getTransactionId(),
                        TransactionStatus.FINAL_FAILED,
                        "Rejected due to timestamp window violation"
                ));

                continue;
            }

            
            if (tx.getSignature() == null) {

                tx.setStatus(TransactionStatus.FINAL_FAILED);
                tx.setFailureReason("MISSING SIGNATURE - invalid packet");

                transactionRepository.save(tx);
                continue;
            }

            
            try {
                String data = tx.canonicalString();

                boolean validSignature = cryptoService.verifyHmac(
                        data,
                        tx.getSignature()
                );

                if (!validSignature) {

                    tx.setStatus(TransactionStatus.FINAL_FAILED);
                    tx.setFailureReason("TAMPER DETECTED - Invalid signature");

                    transactionRepository.save(tx);

                    auditRepository.save(new TransactionAudit(
                            tx.getTransactionId(),
                            TransactionStatus.FINAL_FAILED,
                            "Transaction rejected due to tampering"
                    ));

                    continue;
                }

            } catch (Exception e) {

                tx.setStatus(TransactionStatus.FINAL_FAILED);
                tx.setFailureReason("Signature verification error: " + e.getMessage());

                transactionRepository.save(tx);
                continue;
            }

            
            if (idempotencyRepository.existsById(tx.getTransactionId())) {
                continue;
            }

            try {

                tx.setStatus(TransactionStatus.PROCESSING);
                transactionRepository.save(tx);

                auditRepository.save(new TransactionAudit(
                        tx.getTransactionId(),
                        TransactionStatus.PROCESSING,
                        "Transaction started processing"
                ));

                User sender = userRepository.findByEmail(tx.getFromEmail())
                        .orElseThrow(() -> new RuntimeException("Sender not found"));

                User receiver = userRepository.findByEmail(tx.getToEmail())
                        .orElseThrow(() -> new RuntimeException("Receiver not found"));

                walletService.transferInternal(sender, receiver, tx.getAmount());

                tx.setStatus(TransactionStatus.SUCCESS);
                tx.setTimestamp(LocalDateTime.now());
                tx.setLastUpdated(LocalDateTime.now());

                transactionRepository.save(tx);

                auditRepository.save(new TransactionAudit(
                        tx.getTransactionId(),
                        TransactionStatus.SUCCESS,
                        "Transaction completed successfully"
                ));

                IdempotencyKey key = new IdempotencyKey();
                key.setKey(tx.getTransactionId());
                key.setCreatedAt(LocalDateTime.now());
                idempotencyRepository.save(key);

            } catch (Exception e) {

                tx.setRetryCount(tx.getRetryCount() + 1);

                if (tx.getRetryCount() >= tx.getMaxRetryCount()) {

                    tx.setStatus(TransactionStatus.FINAL_FAILED);

                    auditRepository.save(new TransactionAudit(
                            tx.getTransactionId(),
                            TransactionStatus.FINAL_FAILED,
                            "Max retries reached: " + e.getMessage()
                    ));

                } else {

                    tx.setStatus(TransactionStatus.PENDING);

                    auditRepository.save(new TransactionAudit(
                            tx.getTransactionId(),
                            TransactionStatus.PENDING,
                            "Retrying transaction: attempt " + tx.getRetryCount()
                    ));
                }

                tx.setLastUpdated(LocalDateTime.now());
                transactionRepository.save(tx);
            }
        }
    }
}