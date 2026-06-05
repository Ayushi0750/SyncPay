

package com.project.offline_payment_sync.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class IdempotencyKey {

    @Id
    private String key;

    private LocalDateTime createdAt;

    public IdempotencyKey() {}

    public IdempotencyKey(String key, LocalDateTime createdAt) {
        this.key = key;
        this.createdAt = createdAt;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
