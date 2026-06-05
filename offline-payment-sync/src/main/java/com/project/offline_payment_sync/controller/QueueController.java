
package com.project.offline_payment_sync.controller;

import com.project.offline_payment_sync.dto.QueueStoreRequest;
import com.project.offline_payment_sync.entity.PendingTransaction;
import com.project.offline_payment_sync.service.QueueService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/queue")
@CrossOrigin(origins = "http://localhost:5173")  
public class QueueController {

    private final QueueService queueService;

    public QueueController(QueueService queueService) {
        this.queueService = queueService;
    }

    // 🔥 FIX: Add null safety checks (same pattern as WalletController)
    private String getAuthenticatedUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        return authentication.getName();
    }

    @PostMapping("/store")
    public String storeTransaction(
            @RequestBody QueueStoreRequest request) {

        String email = getAuthenticatedUserEmail();

        return queueService.storePendingTransaction(
                email,
                request
        );
    }

    @GetMapping("/pending")
    public List<PendingTransaction> getPendingTransactions() {

        String email = getAuthenticatedUserEmail();

        return queueService.getPendingTransactions(email);
    }
}