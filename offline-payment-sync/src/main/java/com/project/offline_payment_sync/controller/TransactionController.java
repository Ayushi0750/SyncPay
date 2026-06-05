
package com.project.offline_payment_sync.controller;

import com.project.offline_payment_sync.dto.SendMoneyRequest;
import com.project.offline_payment_sync.dto.TransactionResponse;
import com.project.offline_payment_sync.service.TransactionService;
import com.project.offline_payment_sync.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/transaction")
@CrossOrigin(origins = "http://localhost:5173")
public class TransactionController {

    private final TransactionService transactionService;
    private final WalletService walletService;

    public TransactionController(TransactionService transactionService, WalletService walletService) {
        this.transactionService = transactionService;
        this.walletService = walletService;
    }

    
    // routes
    @PostMapping("/send")
    public ResponseEntity<?> sendMoney(@RequestBody SendMoneyRequest request,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String senderEmail = userDetails.getUsername();
            // Using the new method that actually deducts money
            TransactionResponse response = transactionService.sendMoneyWithWallet(senderEmail, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    
    @GetMapping("/history")
    public ResponseEntity<?> getTransactionHistory(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            String userEmail = userDetails.getUsername();
            List<TransactionResponse> history = transactionService.getTransactionHistory(userEmail);
            return ResponseEntity.ok(history);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingTransactions(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            String userEmail = userDetails.getUsername();
            List<TransactionResponse> pending = transactionService.getPendingTransactions(userEmail);
            return ResponseEntity.ok(pending);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }


    @PostMapping("/sync")
    public ResponseEntity<?> syncTransactions(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            String userEmail = userDetails.getUsername();
            List<TransactionResponse> synced = transactionService.syncPendingTransactions(userEmail);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Sync completed");
            response.put("syncedCount", synced.size());
            response.put("transactions", synced);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
}


class ErrorResponse {
    private String error;
    
    public ErrorResponse(String error) {
        this.error = error;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
}