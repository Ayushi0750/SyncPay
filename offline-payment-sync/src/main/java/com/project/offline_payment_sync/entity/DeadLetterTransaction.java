
package com.project.offline_payment_sync.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class DeadLetterTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionId;

    private String fromEmail;
    private String toEmail;
    private Double amount;

    private String failureReason;

    private int retryCount;

    private LocalDateTime failedAt;

    public DeadLetterTransaction() {}

    public DeadLetterTransaction(String transactionId,
                                 String fromEmail,
                                 String toEmail,
                                 Double amount,
                                 String failureReason,
                                 int retryCount) {
        this.transactionId = transactionId;
        this.fromEmail = fromEmail;
        this.toEmail = toEmail;
        this.amount = amount;
        this.failureReason = failureReason;
        this.retryCount = retryCount;
        this.failedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.failedAt == null) {
            this.failedAt = LocalDateTime.now();
        }
    }

    // getters & setters

    public Long getId() { return id; }

    public String getTransactionId() { return transactionId; }

    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getFromEmail() { return fromEmail; }

    public void setFromEmail(String fromEmail) { this.fromEmail = fromEmail; }

    public String getToEmail() { return toEmail; }

    public void setToEmail(String toEmail) { this.toEmail = toEmail; }

    public Double getAmount() { return amount; }

    public void setAmount(Double amount) { this.amount = amount; }

    public String getFailureReason() { return failureReason; }

    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public int getRetryCount() { return retryCount; }

    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    public LocalDateTime getFailedAt() { return failedAt; }

    public void setFailedAt(LocalDateTime failedAt) { this.failedAt = failedAt; }
}