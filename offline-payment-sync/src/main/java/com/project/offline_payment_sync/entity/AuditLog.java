package com.project.offline_payment_sync.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;          
    private String status;          

    private String fromUser;
    private String toUser;

    private Double amount;

    private String referenceId;     
    private LocalDateTime timestamp;

    private String message;         

    public AuditLog() {
        this.timestamp = LocalDateTime.now();
    }

    public AuditLog(String action, String status, String fromUser, String toUser,
                    Double amount, String referenceId, String message) {
        this.action = action;
        this.status = status;
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.amount = amount;
        this.referenceId = referenceId;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    

    public Long getId() { return id; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFromUser() { return fromUser; }
    public void setFromUser(String fromUser) { this.fromUser = fromUser; }

    public String getToUser() { return toUser; }
    public void setToUser(String toUser) { this.toUser = toUser; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}