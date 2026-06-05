

package com.project.offline_payment_sync.entity;

import com.project.offline_payment_sync.entity.enums.TransactionStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String transactionId;

    private String fromEmail;
    private String toEmail;
    private Double amount;

    
    @Column(length = 500)
    private String signature;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    private int retryCount = 0;

    private int maxRetryCount = 3;

    private LocalDateTime timestamp;

    private LocalDateTime lastUpdated;

    @Column(length = 500)
    private String failureReason;

    public Transaction() {}

    public Transaction(String transactionId, String fromEmail, String toEmail, Double amount) {
        this.transactionId = transactionId;
        this.fromEmail = fromEmail;
        this.toEmail = toEmail;
        this.amount = amount;

        this.status = TransactionStatus.PENDING;
        this.retryCount = 0;
        this.maxRetryCount = 3;

        this.timestamp = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }

        if (this.status == null) {
            this.status = TransactionStatus.PENDING;
        }

        if (this.lastUpdated == null) {
            this.lastUpdated = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }

    
    public String canonicalString() {
        return transactionId + "|" +
               fromEmail + "|" +
               toEmail + "|" +
               amount + "|" +
               timestamp;
    }

    

    public Long getId() {
        return id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public String getToEmail() {
        return toEmail;
    }

    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}