package com.project.offline_payment_sync.dto;

public class SendMoneyRequest {
    private String receiverPhone;
    private Double amount;
    private String notes;

    public SendMoneyRequest() {}

    public SendMoneyRequest(String receiverPhone, Double amount, String notes) {
        this.receiverPhone = receiverPhone;
        this.amount = amount;
        this.notes = notes;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}