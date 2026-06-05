

package com.project.offline_payment_sync.dto;

import com.project.offline_payment_sync.entity.enums.TransactionStatus;
import java.time.LocalDateTime;

public class TransactionResponse {
    private String transactionId;
    private TransactionStatus status;
    private String fromEmail;
    private String toEmail;
    private Double amount;
    private LocalDateTime timestamp;

    
    public TransactionResponse(String transactionId, TransactionStatus status) {
        this.transactionId = transactionId;
        this.status = status;
    }

    
    public TransactionResponse(String transactionId, TransactionStatus status, 
                              String fromEmail, String toEmail, 
                              Double amount, LocalDateTime timestamp) {
        this.transactionId = transactionId;
        this.status = status;
        this.fromEmail = fromEmail;
        this.toEmail = toEmail;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}