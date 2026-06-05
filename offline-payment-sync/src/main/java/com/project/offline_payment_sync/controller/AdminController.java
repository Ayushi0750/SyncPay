package com.project.offline_payment_sync.controller;

import com.project.offline_payment_sync.entity.*;
import com.project.offline_payment_sync.service.AdminService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    //routes 
    @GetMapping("/stats/pending")
    public long pending() {
        return adminService.getPendingCount();
    }

    @GetMapping("/stats/success")
    public long success() {
        return adminService.getSuccessCount();
    }

    @GetMapping("/stats/failed")
    public long failed() {
        return adminService.getFailedCount();
    }


    @GetMapping("/failed")
    public List<Transaction> failedTransactions() {
        return adminService.getFailedTransactions();
    }

    @GetMapping("/dlq")
    public List<DeadLetterTransaction> dlq() {
        return adminService.getDeadLetterTransactions();
    }

    //retry routes 
    @PostMapping("/retry/{transactionId}")
    public String retryFailed(@PathVariable String transactionId) {
        return adminService.retryFailedTransaction(transactionId);
    }

   //dlq
    @PostMapping("/dlq/reprocess/{transactionId}")
    public String reprocessDlq(@PathVariable String transactionId) {
        return adminService.reprocessDlq(transactionId);
    }

    //force-reset 
    @PostMapping("/force-reset/{transactionId}")
    public String forceReset(@PathVariable String transactionId) {
        return adminService.forceResetTransaction(transactionId);
    }
}