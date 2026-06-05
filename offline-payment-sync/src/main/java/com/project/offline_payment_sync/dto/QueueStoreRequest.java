


package com.project.offline_payment_sync.dto;

import lombok.Data;

@Data
public class QueueStoreRequest {

    private String transactionId;
    private String toEmail;
    private Double amount;
}
