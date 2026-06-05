package com.project.offline_payment_sync.repository;

import com.project.offline_payment_sync.entity.MockBankAccount;
import com.project.offline_payment_sync.entity.MockBankTransaction;
import com.project.offline_payment_sync.entity.MockBankTransactionStatus;
import com.project.offline_payment_sync.entity.MockBankTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MockBankTransactionRepository
        extends JpaRepository<MockBankTransaction, Long> {

    Optional<MockBankTransaction> findByReferenceId(String referenceId);

    boolean existsByReferenceId(String referenceId);

    
    List<MockBankTransaction> findByBankAccount(MockBankAccount bankAccount);

    
    List<MockBankTransaction> findByBankAccountAndType(
            MockBankAccount bankAccount,
            MockBankTransactionType type
    );

    
    List<MockBankTransaction> findByStatus(MockBankTransactionStatus status);

    
    @Query("SELECT mbt FROM MockBankTransaction mbt " +
           "WHERE mbt.bankAccount = :bankAccount " +
           "AND mbt.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY mbt.createdAt DESC")
    List<MockBankTransaction> findTransactionsByDateRange(
            @Param("bankAccount") MockBankAccount bankAccount,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    
    @Query("SELECT COALESCE(SUM(mbt.amount), 0) FROM MockBankTransaction mbt " +
           "WHERE mbt.bankAccount = :bankAccount " +
           "AND mbt.type = com.project.offline_payment_sync.entity.MockBankTransactionType.DEBIT")
    Double getTotalDebits(@Param("bankAccount") MockBankAccount bankAccount);

    
    @Query("SELECT COALESCE(SUM(mbt.amount), 0) FROM MockBankTransaction mbt " +
           "WHERE mbt.bankAccount = :bankAccount " +
           "AND mbt.type = com.project.offline_payment_sync.entity.MockBankTransactionType.CREDIT")
    Double getTotalCredits(@Param("bankAccount") MockBankAccount bankAccount);
}