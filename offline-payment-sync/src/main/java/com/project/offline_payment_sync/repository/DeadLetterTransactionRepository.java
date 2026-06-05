

package com.project.offline_payment_sync.repository;

import com.project.offline_payment_sync.entity.DeadLetterTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeadLetterTransactionRepository extends JpaRepository<DeadLetterTransaction, Long> {
}
