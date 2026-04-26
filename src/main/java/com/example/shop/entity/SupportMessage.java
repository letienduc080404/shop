package com.example.shop.entity;

import java.time.LocalDateTime;

public class SupportMessage {

    private String orderCode;
    private String senderRole; // "USER" or "ADMIN"
    private String message;
    private LocalDateTime createdAt = LocalDateTime.now();

    public SupportMessage() {}

    public SupportMessage(String orderCode, String senderRole, String message) {
        this.orderCode = orderCode;
        this.senderRole = senderRole;
        this.message = message;
    }

    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }
    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
