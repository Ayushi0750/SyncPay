
package com.project.offline_payment_sync.service;

import com.project.offline_payment_sync.dto.SendMoneyRequest;
import com.project.offline_payment_sync.dto.TransactionResponse;
import com.project.offline_payment_sync.entity.Transaction;
import com.project.offline_payment_sync.entity.User;
import com.project.offline_payment_sync.entity.enums.TransactionStatus;
import com.project.offline_payment_sync.repository.TransactionRepository;
import com.project.offline_payment_sync.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    
    public TransactionResponse sendMoney(String senderEmail, SendMoneyRequest request) {

        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        User receiver = userRepository.findByPhone(request.getReceiverPhone())
                .orElseThrow(() -> new RuntimeException("Receiver not found with phone: " + request.getReceiverPhone()));

        if (request.getAmount() <= 0) {
            throw new RuntimeException("Amount must be greater than 0");
        }

        String transactionId = "TXN" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();

        Transaction transaction = new Transaction(
                transactionId,
                senderEmail,
                receiver.getEmail(),
                request.getAmount()
        );

        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setTimestamp(LocalDateTime.now());

        transactionRepository.save(transaction);

        return new TransactionResponse(transaction.getTransactionId(), transaction.getStatus());
    }

    
    @Transactional
    public TransactionResponse sendMoneyWithWallet(String senderEmail, SendMoneyRequest request) {

        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        User receiver = userRepository.findByPhone(request.getReceiverPhone())
                .orElseThrow(() -> new RuntimeException("Receiver not found with phone: " + request.getReceiverPhone()));

        if (request.getAmount() <= 0) {
            throw new RuntimeException("Amount must be greater than 0");
        }

        if (sender.getBalance() < request.getAmount()) {
            throw new RuntimeException("Insufficient balance");
        }

        sender.setBalance(sender.getBalance() - request.getAmount());
        receiver.setBalance(receiver.getBalance() + request.getAmount());

        userRepository.save(sender);
        userRepository.save(receiver);

        String transactionId = "TXN" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();

        Transaction transaction = new Transaction(
                transactionId,
                senderEmail,
                receiver.getEmail(),
                request.getAmount()
        );

        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setTimestamp(LocalDateTime.now());

        transactionRepository.save(transaction);

        return new TransactionResponse(
                transaction.getTransactionId(),
                transaction.getStatus(),
                transaction.getFromEmail(),
                transaction.getToEmail(),
                transaction.getAmount(),
                transaction.getTimestamp()
        );
    }

    
    @Transactional
    public void recordBankTransaction(String email, Double amount, String type, String referenceId) {
        
        System.out.println("\n🏦 [TRANSACTION SERVICE] Recording bank transaction...");
        System.out.println("   Email: " + email);
        System.out.println("   Amount: " + amount);
        System.out.println("   Type: " + type);
        System.out.println("   ReferenceId: " + referenceId);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String transactionId = "BANK-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
        
        Transaction transaction = new Transaction();
        transaction.setTransactionId(transactionId);
        transaction.setAmount(amount);
        transaction.setTimestamp(LocalDateTime.now());
        
        if (type.equals("CREDIT")) {
            // User added money from bank to wallet
            transaction.setFromEmail("MOCK_BANK");
            transaction.setToEmail(email);
        } else if (type.equals("DEBIT")) {
            // User withdrew money from wallet to bank
            transaction.setFromEmail(email);
            transaction.setToEmail("MOCK_BANK");
        } else {
            throw new RuntimeException("Unknown transaction type: " + type);
        }
        
        transaction.setStatus(TransactionStatus.SUCCESS);
        
        transactionRepository.save(transaction);
        System.out.println("   ✅ Transaction recorded with ID: " + transactionId);
    }

    
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionHistory(String userEmail) {

        return transactionRepository
                .findByFromEmailOrToEmailOrderByTimestampDesc(userEmail, userEmail)
                .stream()
                .map(tx -> new TransactionResponse(
                        tx.getTransactionId(),
                        tx.getStatus(),
                        tx.getFromEmail(),
                        tx.getToEmail(),
                        tx.getAmount(),
                        tx.getTimestamp()
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getPendingTransactions(String userEmail) {

        return transactionRepository
                .findByFromEmailAndStatus(userEmail, TransactionStatus.PENDING)
                .stream()
                .map(tx -> new TransactionResponse(
                        tx.getTransactionId(),
                        tx.getStatus(),
                        tx.getFromEmail(),
                        tx.getToEmail(),
                        tx.getAmount(),
                        tx.getTimestamp()
                ))
                .collect(Collectors.toList());
    }

    
    @Transactional
    public List<TransactionResponse> syncPendingTransactions(String userEmail) {

        List<Transaction> pendingTransactions =
                transactionRepository.findByFromEmailAndStatus(userEmail, TransactionStatus.PENDING);

        return pendingTransactions.stream()
                .map(tx -> {
                    try {
                        User sender = userRepository.findByEmail(tx.getFromEmail())
                                .orElseThrow(() -> new RuntimeException("Sender not found"));

                        User receiver = userRepository.findByEmail(tx.getToEmail())
                                .orElseThrow(() -> new RuntimeException("Receiver not found"));

                        if (sender.getBalance() >= tx.getAmount()) {

                            sender.setBalance(sender.getBalance() - tx.getAmount());
                            receiver.setBalance(receiver.getBalance() + tx.getAmount());

                            userRepository.save(sender);
                            userRepository.save(receiver);

                            tx.setStatus(TransactionStatus.SUCCESS);
                        } else {
                            tx.setStatus(TransactionStatus.FAILED);
                        }

                        transactionRepository.save(tx);

                        return new TransactionResponse(
                                tx.getTransactionId(),
                                tx.getStatus(),
                                tx.getFromEmail(),
                                tx.getToEmail(),
                                tx.getAmount(),
                                tx.getTimestamp()
                        );

                    } catch (Exception e) {

                        tx.setStatus(TransactionStatus.FAILED);
                        transactionRepository.save(tx);

                        return new TransactionResponse(
                                tx.getTransactionId(),
                                tx.getStatus(),
                                tx.getFromEmail(),
                                tx.getToEmail(),
                                tx.getAmount(),
                                tx.getTimestamp()
                        );
                    }
                })
                .collect(Collectors.toList());
    }
}