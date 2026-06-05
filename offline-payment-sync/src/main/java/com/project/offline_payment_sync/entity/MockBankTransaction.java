package com.project.offline_payment_sync.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mock_bank_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockBankTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id", nullable = false)
    private MockBankAccount bankAccount;

    @Column(nullable = false)
    private Double amount;

    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MockBankTransactionType type;

    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MockBankTransactionStatus status;

    
    private String description;

    
    private String referenceId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {

        this.createdAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = MockBankTransactionStatus.SUCCESS;
        }
    }
}