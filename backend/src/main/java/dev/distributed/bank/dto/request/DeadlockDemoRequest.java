package dev.distributed.bank.dto.request;

import java.math.BigDecimal;

public class DeadlockDemoRequest {

    private String branchId;
    private Long accountAId;
    private Long accountBId;
    private BigDecimal amountAtoB;
    private BigDecimal amountBtoA;

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public Long getAccountAId() {
        return accountAId;
    }

    public void setAccountAId(Long accountAId) {
        this.accountAId = accountAId;
    }

    public Long getAccountBId() {
        return accountBId;
    }

    public void setAccountBId(Long accountBId) {
        this.accountBId = accountBId;
    }

    public BigDecimal getAmountAtoB() {
        return amountAtoB;
    }

    public void setAmountAtoB(BigDecimal amountAtoB) {
        this.amountAtoB = amountAtoB;
    }

    public BigDecimal getAmountBtoA() {
        return amountBtoA;
    }

    public void setAmountBtoA(BigDecimal amountBtoA) {
        this.amountBtoA = amountBtoA;
    }
}
