

package com.project.offline_payment_sync.dto;

import lombok.Data;

@Data
public class TransferRequest {

    private String receiverEmail;
    private Double amount;
}
