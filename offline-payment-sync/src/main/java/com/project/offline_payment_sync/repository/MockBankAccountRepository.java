package com.project.offline_payment_sync.repository;

import com.project.offline_payment_sync.entity.MockBankAccount;
import com.project.offline_payment_sync.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MockBankAccountRepository
        extends JpaRepository<MockBankAccount, Long> {

    Optional<MockBankAccount> findByUser(User user);

    Optional<MockBankAccount> findByUserId(Long userId);

    Optional<MockBankAccount> findByAccountNumber(String accountNumber);

    boolean existsByUser(User user);
}