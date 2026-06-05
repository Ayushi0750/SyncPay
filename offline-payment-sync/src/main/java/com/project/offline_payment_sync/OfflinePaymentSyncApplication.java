package com.project.offline_payment_sync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OfflinePaymentSyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(OfflinePaymentSyncApplication.class, args);
    }
}