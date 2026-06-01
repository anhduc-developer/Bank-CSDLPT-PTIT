package dev.distributed.bank.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionHistory {

    private Long transactionId;
    private String transactionType;
    private BigDecimal amount;
    private Long accountId;
    private Long relatedAccountId;
    private String relatedBranchId;
    private BigDecimal balanceAfter;
    private String status;
    private String distributedTxnId;
    private String description;
    private LocalDateTime createdAt;

    public TransactionHistory() {
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getRelatedAccountId() {
        return relatedAccountId;
    }

    public void setRelatedAccountId(Long relatedAccountId) {
        this.relatedAccountId = relatedAccountId;
    }

    public String getRelatedBranchId() {
        return relatedBranchId;
    }

    public void setRelatedBranchId(String relatedBranchId) {
        this.relatedBranchId = relatedBranchId;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDistributedTxnId() {
        return distributedTxnId;
    }

    public void setDistributedTxnId(String distributedTxnId) {
        this.distributedTxnId = distributedTxnId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}