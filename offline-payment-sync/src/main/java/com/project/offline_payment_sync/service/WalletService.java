
package com.project.offline_payment_sync.service;

import com.project.offline_payment_sync.dto.OfflineTransferRequest;
import com.project.offline_payment_sync.dto.TransferRequest;
import com.project.offline_payment_sync.entity.Transaction;
import com.project.offline_payment_sync.entity.User;
import com.project.offline_payment_sync.entity.enums.TransactionStatus;
import com.project.offline_payment_sync.repository.TransactionRepository;
import com.project.offline_payment_sync.repository.UserRepository;
import com.project.offline_payment_sync.security.AESEncryptionService;
import com.project.offline_payment_sync.security.CryptoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class WalletService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final CryptoService cryptoService;
    private final AESEncryptionService aesEncryptionService;
    private final MockBankService mockBankService;

    public WalletService(UserRepository userRepository,
                         TransactionRepository transactionRepository,
                         CryptoService cryptoService,
                         AESEncryptionService aesEncryptionService,
                         MockBankService mockBankService) {

        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.cryptoService = cryptoService;
        this.aesEncryptionService = aesEncryptionService;
        this.mockBankService = mockBankService;
    }

    @Transactional
    public void transferInternal(User sender, User receiver, Double amount) {

        if (amount <= 0)
            throw new RuntimeException("Invalid amount");

        if (sender.getBalance() < amount)
            throw new RuntimeException("Insufficient balance");

        sender.setBalance(sender.getBalance() - amount);
        receiver.setBalance(receiver.getBalance() + amount);

        userRepository.save(sender);
        userRepository.save(receiver);
    }

    
    @Transactional(readOnly = true)
    public Double getBalance(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getBalance();
    }

    @Transactional
    public String addMoney(String email, Double amount) {

        System.out.println("\n💰 [WALLET SERVICE] Adding money...");
        System.out.println("   Email: " + email);
        System.out.println("   Amount: " + amount);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (amount <= 0) {
            throw new RuntimeException("Amount must be greater than 0");
        }

        try {
            // Step 1: CREDIT Mock Bank Account (FIXED - was debitAccount)
            System.out.println("   [STEP 1] Crediting mock bank account...");
            mockBankService.creditAccount(email, amount);
            System.out.println("    Mock bank credited successfully");

            // Step 2: Credit Wallet
            System.out.println("   [STEP 2] Crediting wallet...");
            user.setBalance(user.getBalance() + amount);
            userRepository.save(user);
            System.out.println("    Wallet credited. New balance: " + user.getBalance());

            // Step 3: Create Transaction Audit Trail
            System.out.println("   [STEP 3] Creating transaction record...");
            Transaction tx = new Transaction();
            tx.setTransactionId("TOPUP-" + System.currentTimeMillis());
            tx.setFromEmail("MOCK_BANK");
            tx.setToEmail(email);
            tx.setAmount(amount);
            tx.setTimestamp(LocalDateTime.now());
            tx.setStatus(TransactionStatus.SUCCESS); 

            String canonical = tx.canonicalString();
            System.out.println("   Canonical string: " + canonical);

            // Step 4: Generate HMAC signature
            System.out.println("   [STEP 4] Generating HMAC signature...");
            tx.setSignature(cryptoService.generateHmac(canonical));
            System.out.println("    HMAC signature generated");

            // Step 5: Encrypt transaction data
            System.out.println("   [STEP 5] Encrypting transaction data...");
            String encryptedData = aesEncryptionService.encrypt(canonical);
            System.out.println("    Encryption successful");

            // Step 6: Save transaction
            System.out.println("   [STEP 6] Saving transaction record...");
            transactionRepository.save(tx);
            System.out.println("    Transaction saved with ID: " + tx.getTransactionId());

            String successMessage = "Money added from Mock Bank. Wallet Balance: " + user.getBalance();
            System.out.println("    SUCCESS: " + successMessage + "\n");
            return successMessage;

        } catch (Exception e) {
            System.out.println("    ERROR: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to add money: " + e.getMessage(), e);
        }
    }

    
    @Transactional
    public String withdrawMoney(String email, Double amount) {

        System.out.println("\n [WALLET SERVICE] Withdrawing money...");
        System.out.println("   Email: " + email);
        System.out.println("   Amount: " + amount);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (amount <= 0) {
            throw new RuntimeException("Amount must be greater than 0");
        }

        if (user.getBalance() < amount) {
            throw new RuntimeException("Insufficient wallet balance");
        }

        try {
            // Step 1: Debit from Wallet
            System.out.println("   [STEP 1] Debiting wallet...");
            user.setBalance(user.getBalance() - amount);
            userRepository.save(user);
            System.out.println("    Wallet debited. New balance: " + user.getBalance());

            // Step 2: Credit Mock Bank Account (user gets money back in bank)
            System.out.println("   [STEP 2] Crediting mock bank account...");
            mockBankService.creditAccount(email, amount);
            System.out.println("    Mock bank credited successfully");

            // Step 3: Create Transaction Audit Trail
            System.out.println("   [STEP 3] Creating transaction record...");
            Transaction tx = new Transaction();
            tx.setTransactionId("WITHDRAW-" + System.currentTimeMillis());
            tx.setFromEmail(email);
            tx.setToEmail("MOCK_BANK");
            tx.setAmount(amount);
            tx.setTimestamp(LocalDateTime.now());
            tx.setStatus(TransactionStatus.SUCCESS);  

            String canonical = tx.canonicalString();
            System.out.println("   Canonical string: " + canonical);

            // Step 4: Generate HMAC signature
            System.out.println("   [STEP 4] Generating HMAC signature...");
            tx.setSignature(cryptoService.generateHmac(canonical));
            System.out.println("    HMAC signature generated");

            // Step 5: Encrypt transaction data
            System.out.println("   [STEP 5] Encrypting transaction data...");
            String encryptedData = aesEncryptionService.encrypt(canonical);
            System.out.println("    Encryption successful");

            // Step 6: Save transaction
            System.out.println("   [STEP 6] Saving transaction record...");
            transactionRepository.save(tx);
            System.out.println("    Transaction saved with ID: " + tx.getTransactionId());

            String successMessage = "Money withdrawn to Mock Bank. Wallet Balance: " + user.getBalance();
            System.out.println("    SUCCESS: " + successMessage + "\n");
            return successMessage;

        } catch (Exception e) {
            System.out.println("   ERROR: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to withdraw money: " + e.getMessage(), e);
        }
    }

    // =========================
    // ONLINE TRANSFER
    // =========================
    @Transactional
    public String transferMoney(String email, TransferRequest request) {

        User sender = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        User receiver = userRepository.findByEmail(request.getReceiverEmail())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        transferInternal(sender, receiver, request.getAmount());

        Transaction tx = new Transaction();
        tx.setTransactionId("TXN-" + System.currentTimeMillis());
        tx.setFromEmail(email);
        tx.setToEmail(request.getReceiverEmail());
        tx.setAmount(request.getAmount());
        tx.setTimestamp(LocalDateTime.now());
        tx.setStatus(TransactionStatus.SUCCESS);  

        String canonical = tx.canonicalString();
        tx.setSignature(cryptoService.generateHmac(canonical));

        transactionRepository.save(tx);

        return "Transfer successful";
    }

    
    @Transactional
    public String syncTransfer(String email, OfflineTransferRequest request) {

        if (transactionRepository.existsByTransactionId(
                request.getTransactionId())) {

            return "Duplicate transaction ignored (idempotent safe)";
        }

        User sender = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        User receiver = userRepository.findByEmail(request.getToEmail())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        transferInternal(sender, receiver, request.getAmount());

        Transaction tx = new Transaction();
        tx.setTransactionId(request.getTransactionId());
        tx.setFromEmail(email);
        tx.setToEmail(request.getToEmail());
        tx.setAmount(request.getAmount());
        tx.setTimestamp(LocalDateTime.now());
        tx.setStatus(TransactionStatus.SUCCESS);  

        String canonical = tx.canonicalString();
        tx.setSignature(cryptoService.generateHmac(canonical));

        transactionRepository.save(tx);

        return "Offline sync transfer successful";
    }
}