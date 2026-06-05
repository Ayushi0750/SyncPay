
package com.project.offline_payment_sync.service;

import com.project.offline_payment_sync.entity.*;
import com.project.offline_payment_sync.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class MockBankService {

    private final MockBankAccountRepository mockBankAccountRepository;
    private final MockBankTransactionRepository mockBankTransactionRepository;
    private final UserRepository userRepository;
    private final IdempotencyService idempotencyService;
    private final AuditLogService auditLogService;
    private final TransactionService transactionService; 

    public MockBankService(
            MockBankAccountRepository mockBankAccountRepository,
            MockBankTransactionRepository mockBankTransactionRepository,
            UserRepository userRepository,
            IdempotencyService idempotencyService,
            AuditLogService auditLogService,
            TransactionService transactionService  
    ) {
        this.mockBankAccountRepository = mockBankAccountRepository;
        this.mockBankTransactionRepository = mockBankTransactionRepository;
        this.userRepository = userRepository;
        this.idempotencyService = idempotencyService;
        this.auditLogService = auditLogService;
        this.transactionService = transactionService;  
    }

    private MockBankAccount getOrCreateAccount(User user) {
        return mockBankAccountRepository.findByUser(user)
                .orElseGet(() -> {
                    MockBankAccount account = MockBankAccount.builder()
                            .user(user)
                            .balance(100000.0)
                            .build();
                    return mockBankAccountRepository.save(account);
                });
    }

    
    @Transactional(readOnly = true)
    public Double getBankBalance(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return getOrCreateAccount(user).getBalance();
    }

   
    @Transactional
    public void creditAccount(String email, Double amount, String referenceId, boolean forceFail) {

        if (forceFail) {
            auditLogService.logFailure("CREDIT", email, null, amount, referenceId, "Forced failure");
            throw new RuntimeException("Simulated credit failure");
        }

        creditInternal(email, amount, referenceId);
    }

    
    @Transactional
    public void debitAccount(String email, Double amount, String referenceId, boolean forceFail) {

        if (forceFail) {
            auditLogService.logFailure("DEBIT", email, null, amount, referenceId, "Forced failure");
            throw new RuntimeException("Simulated debit failure");
        }

        debitInternal(email, amount, referenceId);
    }

    
    @Transactional
    public void debitAccount(String email, Double amount) {
        String referenceId = generateReferenceId();
        debitInternal(email, amount, referenceId);
    }

    @Transactional
    public void creditAccount(String email, Double amount) {
        String referenceId = generateReferenceId();
        creditInternal(email, amount, referenceId);
    }

    
    private void creditInternal(String email, Double amount, String referenceId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        MockBankAccount account = getOrCreateAccount(user);

        idempotencyService.validateNewTransaction(referenceId);

        // 1. Update Mock Bank Account balance
        account.setBalance(account.getBalance() + amount);
        mockBankAccountRepository.save(account);

        // 2.  UPDATE USER WALLET BALANCE
        user.setBalance(user.getBalance() + amount);
        userRepository.save(user);

        // 3. Save bank transaction record
        MockBankTransaction tx = MockBankTransaction.builder()
                .bankAccount(account)
                .amount(amount)
                .type(MockBankTransactionType.CREDIT)
                .status(MockBankTransactionStatus.SUCCESS)
                .referenceId(referenceId)
                .description("Wallet Credit")
                .build();

        mockBankTransactionRepository.save(tx);

        // 4.  RECORD IN MAIN TRANSACTION TABLE
        transactionService.recordBankTransaction(email, amount, "CREDIT", referenceId);

        // 5. Audit log
        auditLogService.logSuccess("CREDIT", email, null, amount, referenceId, "Credited successfully");
    }

    private void debitInternal(String email, Double amount, String referenceId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        MockBankAccount account = getOrCreateAccount(user);

        idempotencyService.validateNewTransaction(referenceId);

        if (account.getBalance() < amount) {
            auditLogService.logFailure("DEBIT", email, null, amount, referenceId, "Insufficient balance");
            throw new RuntimeException("Insufficient balance");
        }

        // 1. Update Mock Bank Account balance
        account.setBalance(account.getBalance() - amount);
        mockBankAccountRepository.save(account);

        // 2. UPDATE USER WALLET BALANCE
        user.setBalance(user.getBalance() - amount);
        userRepository.save(user);

        // 3. Save bank transaction record
        MockBankTransaction tx = MockBankTransaction.builder()
                .bankAccount(account)
                .amount(amount)
                .type(MockBankTransactionType.DEBIT)
                .status(MockBankTransactionStatus.SUCCESS)
                .referenceId(referenceId)
                .description("Wallet Debit")
                .build();

        mockBankTransactionRepository.save(tx);

        // 4.  RECORD IN MAIN TRANSACTION TABLE
        transactionService.recordBankTransaction(email, amount, "DEBIT", referenceId);

        // 5. Audit log
        auditLogService.logSuccess("DEBIT", email, null, amount, referenceId, "Debited successfully");
    }

    private String generateReferenceId() {
        return "MBTX-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}