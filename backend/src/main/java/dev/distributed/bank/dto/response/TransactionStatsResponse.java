package dev.distributed.bank.dto.response;

import java.math.BigDecimal;

public class TransactionStatsResponse {

    private String branchId;
    private String branchName;
    private long totalTransactions;
    private long depositCount;
    private long withdrawCount;
    private long transferCount;
    private BigDecimal totalDepositAmount;
    private BigDecimal totalWithdrawAmount;

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public long getDepositCount() {
        return depositCount;
    }

    public void setDepositCount(long depositCount) {
        this.depositCount = depositCount;
    }

    public long getWithdrawCount() {
        return withdrawCount;
    }

    public void setWithdrawCount(long withdrawCount) {
        this.withdrawCount = withdrawCount;
    }

    public long getTransferCount() {
        return transferCount;
    }

    public void setTransferCount(long transferCount) {
        this.transferCount = transferCount;
    }

    public BigDecimal getTotalDepositAmount() {
        return totalDepositAmount;
    }

    public void setTotalDepositAmount(BigDecimal totalDepositAmount) {
        this.totalDepositAmount = totalDepositAmount;
    }

    public BigDecimal getTotalWithdrawAmount() {
        return totalWithdrawAmount;
    }

    public void setTotalWithdrawAmount(BigDecimal totalWithdrawAmount) {
        this.totalWithdrawAmount = totalWithdrawAmount;
    }
}
