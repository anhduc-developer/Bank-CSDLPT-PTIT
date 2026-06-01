package dev.distributed.bank.dto.request;

import java.math.BigDecimal;

public class WithdrawRequest {

    private Long accountId;
    private String branchId;
    private BigDecimal amount;
    private BigDecimal amountThread1;
    private BigDecimal amountThread2;
    private boolean useLock = true;

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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmountThread1() {
        return amountThread1;
    }

    public void setAmountThread1(BigDecimal amountThread1) {
        this.amountThread1 = amountThread1;
    }

    public BigDecimal getAmountThread2() {
        return amountThread2;
    }

    public void setAmountThread2(BigDecimal amountThread2) {
        this.amountThread2 = amountThread2;
    }

    public boolean isUseLock() {
        return useLock;
    }

    public void setUseLock(boolean useLock) {
        this.useLock = useLock;
    }
}
