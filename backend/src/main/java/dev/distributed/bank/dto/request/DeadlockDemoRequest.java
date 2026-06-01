package dev.distributed.bank.dto.request;

import java.math.BigDecimal;

/**
 * DTO: Request cho demo Deadlock.
 * 
 * Client gửi JSON body này khi gọi POST /api/demo/deadlock
 * Cần 2 tài khoản cùng chi nhánh để tạo deadlock trên cùng 1 database.
 */
public class DeadlockDemoRequest {

    private String branchId; // Chi nhánh (HN, DN, HCM)
    private Long accountAId; // Tài khoản A
    private Long accountBId; // Tài khoản B
    private BigDecimal amountAtoB; // Số tiền Thread-1: A → B
    private BigDecimal amountBtoA; // Số tiền Thread-2: B → A

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
