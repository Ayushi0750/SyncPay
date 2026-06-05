package com.project.offline_payment_sync.service;

import com.project.offline_payment_sync.repository.MockBankTransactionRepository;
import org.springframework.stereotype.Service;

@Service
public class IdempotencyService {

    private final MockBankTransactionRepository mockBankTransactionRepository;

    public IdempotencyService(MockBankTransactionRepository mockBankTransactionRepository) {
        this.mockBankTransactionRepository = mockBankTransactionRepository;
    }

    
    public boolean isDuplicate(String referenceId) {

        if (referenceId == null || referenceId.isEmpty()) {
            throw new RuntimeException("ReferenceId cannot be null or empty");
        }

        return mockBankTransactionRepository.existsByReferenceId(referenceId);
    }

   
    public void validateNewTransaction(String referenceId) {

        if (isDuplicate(referenceId)) {
            System.out.println("⚠ [IDEMPOTENCY] Duplicate transaction blocked: " + referenceId);
            throw new RuntimeException("Duplicate transaction detected (idempotency block)");
        }
    }
}