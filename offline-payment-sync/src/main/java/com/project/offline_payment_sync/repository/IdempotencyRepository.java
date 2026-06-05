package com.project.offline_payment_sync.repository;

import com.project.offline_payment_sync.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyRepository extends JpaRepository<IdempotencyKey, String> {
}