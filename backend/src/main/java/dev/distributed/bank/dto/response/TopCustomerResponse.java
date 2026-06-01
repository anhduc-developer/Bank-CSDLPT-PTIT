package dev.distributed.bank.dto.response;

import java.math.BigDecimal;

public class TopCustomerResponse {

    private Long customerId;
    private String fullName;
    private String branchId;
    private BigDecimal totalBalance;
    private int accountCount;

    public TopCustomerResponse() {
    }

    public TopCustomerResponse(Long customerId, String fullName, String branchId,
            BigDecimal totalBalance, int accountCount) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.branchId = branchId;
        this.totalBalance = totalBalance;
        this.accountCount = accountCount;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public BigDecimal getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(BigDecimal totalBalance) {
        this.totalBalance = totalBalance;
    }

    public int getAccountCount() {
        return accountCount;
    }

    public void setAccountCount(int accountCount) {
        this.accountCount = accountCount;
    }
}
