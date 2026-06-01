package dev.distributed.bank.dto.response;

import java.math.BigDecimal;

public class TopTransactionCustomerResponse {

    private Long customerId;
    private String fullName;
    private String branchId;
    private BigDecimal totalAmount;
    private int transactionCount;

    public TopTransactionCustomerResponse() {
    }

    public TopTransactionCustomerResponse(Long customerId, String fullName, String branchId,
            BigDecimal totalAmount, int transactionCount) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.branchId = branchId;
        this.totalAmount = totalAmount;
        this.transactionCount = transactionCount;
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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(int transactionCount) {
        this.transactionCount = transactionCount;
    }
}
