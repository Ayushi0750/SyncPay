package com.project.offline_payment_sync.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "mock_bank_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockBankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

   
     
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    
    @Column(nullable = false, unique = true)
    private String accountNumber;

    
    @Column(nullable = false)
    private Double balance;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {

        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        // Auto-generate account number if not provided
        if (this.accountNumber == null) {
            this.accountNumber = "MB" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        }

        
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    
    public void credit(Double amount) {
        this.balance = this.balance + amount;
    }

    public void debit(Double amount) {
        this.balance = this.balance - amount;
    }
}