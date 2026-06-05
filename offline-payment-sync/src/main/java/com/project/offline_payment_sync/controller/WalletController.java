

package com.project.offline_payment_sync.controller;

import com.project.offline_payment_sync.dto.OfflineTransferRequest;
import com.project.offline_payment_sync.dto.TransferRequest;
import com.project.offline_payment_sync.service.WalletService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/wallet")
@CrossOrigin(origins = "http://localhost:5173")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    private String getAuthenticatedUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        return authentication.getName();
    }

   //routes
    @PostMapping("/add")
    public ResponseEntity<?> addMoney(@RequestParam Double amount) {
        try {
            System.out.println("\n [WALLET CONTROLLER] POST /wallet/add");
            System.out.println("   Amount: " + amount);
            
            String email = getAuthenticatedUserEmail();
            System.out.println("   Authenticated user: " + email);
            System.out.println("   Authorities: " + SecurityContextHolder.getContext().getAuthentication().getAuthorities());
            
            String result = walletService.addMoney(email, amount);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", result);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("    ERROR: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    
    @GetMapping("/balance")
    public ResponseEntity<?> getBalance() {
        try {
            System.out.println("\n [WALLET CONTROLLER] GET /wallet/balance");
            
            String email = getAuthenticatedUserEmail();
            System.out.println("   Authenticated user: " + email);
            
            Double balance = walletService.getBalance(email);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("balance", balance);
            response.put("email", email);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("    ERROR: " + e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transferMoney(@RequestBody TransferRequest request) {
        try {
            System.out.println("\n  [WALLET CONTROLLER] POST /wallet/transfer");
            
            String email = getAuthenticatedUserEmail();
            System.out.println("   From: " + email);
            System.out.println("   To: " + request.getReceiverEmail());
            System.out.println("   Amount: " + request.getAmount());
            
            String result = walletService.transferMoney(email, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", result);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("    ERROR: " + e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    
    @PostMapping("/sync-transfer")
    public ResponseEntity<?> syncTransfer(@RequestBody OfflineTransferRequest request) {
        try {
            System.out.println("\n  [WALLET CONTROLLER] POST /wallet/sync-transfer");
            
            String email = getAuthenticatedUserEmail();
            System.out.println("   From: " + email);
            System.out.println("   To: " + request.getToEmail());
            System.out.println("   Amount: " + request.getAmount());
            System.out.println("   Transaction ID: " + request.getTransactionId());
            
            String result = walletService.syncTransfer(email, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", result);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("    ERROR: " + e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}