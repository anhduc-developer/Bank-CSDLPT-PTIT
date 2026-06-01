package dev.distributed.bank.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Account {

    private Long accountId;
    private Long customerId;
    private String branchId;
    private BigDecimal balance;
    private String status;
    private LocalDateTime createdAt;

    public Account() {
    }

    public Account(Long customerId, String branchId, BigDecimal balance) {
        this.customerId = customerId;
        this.branchId = branchId;
        this.balance = balance;
        this.status = "ACTIVE";
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
