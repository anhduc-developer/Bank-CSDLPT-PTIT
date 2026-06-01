package dev.distributed.bank.dto.response;

import java.math.BigDecimal;

public class BalanceResponse {

    private Long accountId;
    private String branchId;
    private BigDecimal balance;
    private String customerName;
    private String status;

    public BalanceResponse() {
    }

    public BalanceResponse(Long accountId, String branchId, BigDecimal balance,
            String customerName, String status) {
        this.accountId = accountId;
        this.branchId = branchId;
        this.balance = balance;
        this.customerName = customerName;
        this.status = status;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
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

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
