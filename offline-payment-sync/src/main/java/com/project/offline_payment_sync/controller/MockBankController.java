package com.project.offline_payment_sync.controller;

import com.project.offline_payment_sync.service.MockBankService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/mockbank")
@CrossOrigin(origins = "http://localhost:5173")
public class MockBankController {

    private final MockBankService mockBankService;

    public MockBankController(MockBankService mockBankService) {
        this.mockBankService = mockBankService;
    }

    @PostMapping("/credit")
    public ResponseEntity<?> credit(@RequestBody Map<String, Object> request) {

        String email = getAuthenticatedUserEmail();
        Double amount = Double.valueOf(request.get("amount").toString());
        String referenceId = (String) request.get("referenceId");
        boolean forceFail = request.get("forceFail") != null &&
                Boolean.parseBoolean(request.get("forceFail").toString());

        System.out.println("\n💰 [MOCK BANK CONTROLLER] CREDIT");
        System.out.println("   Authenticated user: " + email);
        System.out.println("   Amount: " + amount);
        System.out.println("   Force Fail: " + forceFail);

        mockBankService.creditAccount(email, amount, referenceId, forceFail);

        return ResponseEntity.ok(Map.of(
                "message", "Credit successful",
                "email", email,
                "amount", amount
        ));
    }

    
    @PostMapping("/debit")
    public ResponseEntity<?> debit(@RequestBody Map<String, Object> request) {

        String email = getAuthenticatedUserEmail();
        Double amount = Double.valueOf(request.get("amount").toString());
        String referenceId = (String) request.get("referenceId");
        boolean forceFail = request.get("forceFail") != null &&
                Boolean.parseBoolean(request.get("forceFail").toString());

        System.out.println("\n🏧 [MOCK BANK CONTROLLER] DEBIT");
        System.out.println("   Authenticated user: " + email);
        System.out.println("   Amount: " + amount);
        System.out.println("   Force Fail: " + forceFail);

        mockBankService.debitAccount(email, amount, referenceId, forceFail);

        return ResponseEntity.ok(Map.of(
                "message", "Debit successful",
                "email", email,
                "amount", amount
        ));
    }

    
    @GetMapping("/balance")
    public ResponseEntity<?> balance(@RequestParam String email) {

        Double balance = mockBankService.getBankBalance(email);

        return ResponseEntity.ok(Map.of(
                "balance", balance,
                "email", email
        ));
    }

    
    private String getAuthenticatedUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            System.err.println("❌ No authenticated user found!");
            throw new RuntimeException("User not authenticated");
        }
        
        String email = authentication.getName();
        System.out.println("🔐 Authenticated user email: " + email);
        
        return email;
    }
}